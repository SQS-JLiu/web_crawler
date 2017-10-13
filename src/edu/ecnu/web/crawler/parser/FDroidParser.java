package edu.ecnu.web.crawler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ecnu.web.crawler.db.FDroidDBControl;
import edu.ecnu.web.crawler.model.DroidAppDto;
import edu.ecnu.web.crawler.model.DroidCommentDto;
import edu.ecnu.web.crawler.model.DroidIssueDto;
import edu.ecnu.web.crawler.utils.HttpUtils;

public class FDroidParser implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(FDroidParser.class);

    private static LinkedBlockingQueue<DroidAppDto> appUrlMQ = new LinkedBlockingQueue<DroidAppDto>();

    private static LinkedBlockingQueue<DroidAppDto> appIssuesMQ = new LinkedBlockingQueue<DroidAppDto>();

    private String baseUrl;

    public FDroidParser() {
    }

    public FDroidParser(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void run() {

    }

    /**
     * 解析出app packages一个html页面的app应用的信息(http://f-droid.org/packages)
     * 
     * @param url
     * @param html
     * @return
     */
    public List<DroidAppDto> parsePackageInfo(String url, String html) {
        // 获取的数据，存放在集合中
        List<DroidAppDto> dataList = new ArrayList<DroidAppDto>();
        // 采用Jsoup解析
        Document doc = Jsoup.parse(html);
        // 获取html标签中的内容
        Elements elements = doc.select("div[class=post-content]").select("a[class=package-header]");
        DroidAppDto appModel;
        String appName, describe, lisence, appUrl;
        String[] tempArry;
        try {
            for (Element ele : elements) {
                appName = ele.select("div[class=package-info]").select("h4").text();
                describe = ele.select("div[class=package-info]").select("div[class=package-desc]")
                    .select("span[class=package-summary]").text();
                lisence = ele.select("div[class=package-info]").select("div[class=package-desc]")
                    .select("span[class=package-license]").text();
                tempArry = ele.attr("href").split("/packages/");
                appModel = new DroidAppDto();
                appModel.setAppName(appName);
                appModel.setDescribe(describe);
                if (tempArry != null && tempArry.length > 1) {
                    appUrl = url + tempArry[1];
                    appModel.setAppUrl(appUrl);
                }
                // 将每一个对象的值，保存到List集合中
                dataList.add(appModel);
                System.out.println("AppName: " + appModel.getAppName() + "\nDescribe: " + appModel.getDescribe()
                    + "\nLisence: " + lisence + "\nAppUrl: " + appModel.getAppUrl() + "\n\n");
            }
        }
        catch (Exception e) {
            logger.error(e.toString());
        }
        return dataList;
    }

    /**
     * 获取所有APP应用的总共页数
     */
    public void getAppTotalPages() {
        String url = "https://f-droid.org/packages";
        HttpResponse response = HttpUtils.getRawHtml(url);
        // 获取响应状态码
        int StatusCode = response.getStatusLine().getStatusCode();
        // 如果状态响应码为200，则获取html实体内容或者json文件
        int totalPage = 0;
        try {
            if (StatusCode == 200) {
                String entity;
                if (response.getEntity() == null) {
                    logger.warn("Maybe getting webpage content failed.");
                    return;
                }
                entity = EntityUtils.toString(response.getEntity(), "utf-8");
                Document doc = Jsoup.parse(entity);
                // 获取html标签中的内容
                Elements elements = doc.select("li[class=nav page last]");
                if (elements.hasText()) {
                    totalPage = Integer.parseInt(elements.select("a[class=label]").text());
                }
                else {
                    logger.info("totalPage is " + totalPage, "Maybe getting webpage content failed.");
                }
                EntityUtils.consume(response.getEntity());
                // System.out.println(elements.select("a[class=label]").text() + ":" + totalPage);
            }
            else {
                // 否则，消耗掉实体
                EntityUtils.consume(response.getEntity());
            }
        }
        catch (ParseException pe) {
            logger.error(pe.toString());
        }
        catch (IOException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 通过API，获取所有应用APP的JSON数据，从而得到所有APP数据(http://f-droid.org/js/index.json)
     * 
     * @param url
     */
    public JSONObject parseAllPackageInfo(String api) {
        HttpResponse response = HttpUtils.getRawHtml(api);
        int StatusCode = response.getStatusLine().getStatusCode();
        // 如果状态响应码为200，则获取json文件
        try {
            if (StatusCode == 200) {
                if (response.getEntity() == null) {
                    logger.warn("Maybe getting webpage content failed.");
                    return null;
                }
                String entity;
                entity = EntityUtils.toString(response.getEntity());
                JSONObject appObj = new JSONObject(entity);
                // 所有APP信息
                // System.out.println(appObj.getJSONObject("docs"));
                EntityUtils.consume(response.getEntity());
                return appObj.getJSONObject("docs");
            }
            else {
                // 否则，消耗掉实体
                EntityUtils.consume(response.getEntity());
                logger.warn("Http Request failed.");
            }
        }
        catch (ParseException pe) {
            logger.error("parseAllPackageInfo --> ParseException:[" + pe.toString() + "]");
        }
        catch (IOException e) {
            e.getStackTrace();
            logger.error("parseAllPackageInfo --> IOException:[" + e.toString() + "]");
        }
        return null;
    }

    /**
     * 组装单个APP详细信息的Url
     * 
     * @param url
     * @return
     */
    public boolean assembleAppUrl(String url) {
        JSONObject appObj = parseAllPackageInfo(url);
        if (appObj == null) {
            logger.warn("assembleAppUrl --> Getting apps info failed,maybe network connection is unnormal.");
            return false;
        }
        appUrlMQ.clear();
        @SuppressWarnings("rawtypes")
        Iterator iterator = appObj.keys();
        String key;
        JSONObject oneAppObj;
        DroidAppDto appModel;
        while (iterator.hasNext()) {
            key = (String) iterator.next();
            oneAppObj = appObj.getJSONObject(key);
            appModel = new DroidAppDto();
            appModel.setId(oneAppObj.getInt("id"));
            appModel.setAppName(oneAppObj.getString("name"));
            appModel.setDescribe(oneAppObj.getString("summary"));
            appModel.setPackageName(oneAppObj.getString("packageName"));
            appModel.setAppUrl(baseUrl + oneAppObj.getString("packageName"));
            appUrlMQ.add(appModel);
        }
        System.out.println(appObj.toString());
        return true;
    }

    private boolean parseAppIssuesUrl(DroidAppDto appModel) {
        HttpResponse response = HttpUtils.getRawHtml(appModel.getAppUrl());
        // System.out.println("Debug:" + appModel.getAppUrl());
        int StatusCode = response.getStatusLine().getStatusCode();
        // 如果状态响应码为200，则获取json文件
        try {
            if (StatusCode == 200) {
                if (response.getEntity() == null) {
                    logger.warn("Getting webpage content failed.");
                    return false;
                }
                String entity = EntityUtils.toString(response.getEntity(), "utf-8");
                Document doc = Jsoup.parse(entity);
                // 获取html标签中的内容
                Elements elements = doc.select("article[class=package]").select("p");
                for (Element ele : elements) {
                    Elements es = ele.select("p:contains(Issue Tracker)").select("a");
                    for (Element e : es) {
                        if (e.attr("href") != null && e.attr("href").startsWith("https://github.com")
                            && e.attr("href").endsWith("issues")) {
                            System.out.println("\nIssues href: "
                                + e.attr("href").replace("https://github.com", "https://api.github.com/repos")
                                + "?state=all");
                            appModel.setIssuesUrl(e.attr("href").replace("https://github.com",
                                "https://api.github.com/repos")
                                + "?state=all");
                            appIssuesMQ.add(appModel);
                        }
                    }
                }
                EntityUtils.consume(response.getEntity());
            }
            else {
                // 否则，消耗掉实体
                EntityUtils.consume(response.getEntity());
                logger.warn("Http Request failed. StatusCode is " + StatusCode);
            }
        }
        catch (ParseException pe) {
            logger.error("parseAppIssuesUrl --> ParseException:[" + pe.toString() + "]");
        }
        catch (IOException e) {
            logger.error("parseAppIssuesUrl --> IOException:[" + e.toString() + "]");
        }
        FDroidDBControl.insertAppInfo(appModel);
        return true;
    }

    public void parseAllAppIssuesUrl(String url) {
        boolean status = assembleAppUrl(url);
        if (!status) {
            logger.warn("parseAppIssuesUrl --> Getting apps info failed,maybe connection is unnormal.");
            return;
        }
        appIssuesMQ.clear();
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        // 创建10个线程进行对约1k个APP的issuesUrl进行解析,估计需要执行一段时间
        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    DroidAppDto appModel;
                    while (true) {
                        appModel = appUrlMQ.poll();
                        if (appModel == null) {
                            break;
                        }
                        parseAppIssuesUrl(appModel);
                    }
                }
            });
        }
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            logger.error(e.toString());
        }
        cachedThreadPool.shutdown();
        while (true) {
            // 等待所有线程执行结束
            if (cachedThreadPool.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                logger.error(e.toString());
            }
            catch (SecurityException se) {
                logger.error(se.toString());
            }
        }
        logger.info("Method parseAllAppIssuesUrl() end...");
    }

    public void getAllIssuesOfApp(DroidAppDto appModel) {
        if (appModel.getIssuesUrl() == null) {
            return;
        }
        int curPage = 0, totalPage = 1;
        boolean checkPageFlag = true;
        String issueUrl = appModel.getIssuesUrl();
        do {
            curPage++;
            HttpResponse response = HttpUtils.getRawHtml(issueUrl + "&page=" + curPage, "664838289", "lj664838289");
            int StatusCode = response.getStatusLine().getStatusCode();
            if (StatusCode == 200) {
                if (response.getEntity() == null) {
                    logger.warn("Getting [" + appModel.getIssuesUrl() + "] json data failed.");
                    continue;
                }
                try {
                    if (checkPageFlag) {
                        for (Header s : response.getHeaders("Link")) {
                            totalPage = Integer.valueOf(s.getValue().split("&page=")[2].split(">")[0]);
                        }
                        checkPageFlag = false;
                    }
                    String entity = EntityUtils.toString(response.getEntity(), "utf-8");
                    // logger.info("Url:" + appModel.getIssuesUrl() + "\t entity:" + entity);
                    JSONArray issuesArray = new JSONArray(entity);
                    JSONObject issueObj;
                    DroidIssueDto issue;
                    for (int i = 0; i < issuesArray.length(); i++) {
                        issueObj = issuesArray.getJSONObject(i);
                        if (issueObj.length() < 4) {
                            logger.warn(issueObj.toString());
                            continue;
                        }
                        issue = new DroidIssueDto();
                        issue.setId(appModel.getId());
                        issue.setTitle(issueObj.getString("title"));
                        issue.setBody(issueObj.get("body").toString());
                        issue.setComments_url(issueObj.getString("comments_url"));
                        issue.setComments(issueObj.getInt("comments"));
                        FDroidDBControl.insertIssues(issue);
                        // 暂时不在这里采集，可以单独调用采集
                        // if (issueObj.getInt("comments") > 0) {
                        // getAllCommentsOfIssues(issueObj.getString("comments_url"));
                        // }
                    }
                    EntityUtils.consume(response.getEntity());
                }
                catch (ParseException e) {
                    e.printStackTrace();
                    logger.error("getAllIssuesOfApp --> " + e.toString());
                }
                catch (IOException e) {
                    e.printStackTrace();
                    logger.error("getAllIssuesOfApp --> " + e.toString());
                }
            }
            else {
                logger.warn("Getting [" + appModel.getIssuesUrl() + "] json data failed. ");
            }
        }
        while (curPage < totalPage);
    }

    public void getAllCommentsOfIssues(String commentsUrl) {
        HttpResponse response = HttpUtils.getRawHtml(commentsUrl, "664838289", "lj664838289");
        int StatusCode = response.getStatusLine().getStatusCode();
        if (StatusCode == 200) {
            if (response.getEntity() == null) {
                logger.warn("Getting [" + commentsUrl + "] json data failed.");
                return;
            }
            try {
                String entity = EntityUtils.toString(response.getEntity(), "utf-8");
                JSONArray issuesArray = new JSONArray(entity);
                DroidCommentDto comment;
                JSONObject commentObj;
                for (int i = 0; i < issuesArray.length(); i++) {
                    commentObj = issuesArray.getJSONObject(i);
                    if (!commentObj.has("body")) {
                        logger.warn(commentObj.toString());
                        continue;
                    }
                    comment = new DroidCommentDto();
                    comment.setComments_url(commentsUrl);
                    comment.setBody(commentObj.getString("body"));
                    FDroidDBControl.insertComments(comment);
                }
                EntityUtils.consume(response.getEntity());
            }
            catch (ParseException e) {
                e.printStackTrace();
                logger.error("getAllIssuesOfApp --> " + e.toString());
            }
            catch (IOException e) {
                e.printStackTrace();
                logger.error("getAllIssuesOfApp --> " + e.toString());
            }
        }
        else {
            logger.warn("Getting [" + commentsUrl + "] json data failed. ");
        }
    }

    public void getIssuesOfAllApp() {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        LinkedBlockingQueue<DroidAppDto> issuesQueue = FDroidDBControl.queryAllApp();
        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    DroidAppDto appModel;
                    while (true) {
                        // appModel = appIssuesMQ.poll();
                        appModel = issuesQueue.poll();
                        if (appModel == null) {
                            break;
                        }
                        getAllIssuesOfApp(appModel);
                    }
                }

            });
        }
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            logger.error(e.toString());
        }
        cachedThreadPool.shutdown();
    }

    public void getCommentsOfAllIssue() {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        LinkedBlockingQueue<DroidIssueDto> issuesQueue = FDroidDBControl.queryAllIssues();
        for (int i = 0; i < 20; i++) {
            cachedThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    DroidIssueDto issueDto;
                    while (true) {
                        issueDto = issuesQueue.poll();
                        if (issueDto == null) {
                            break;
                        }
                        if (issueDto.getComments() > 0) {
                            getAllCommentsOfIssues(issueDto.getComments_url());
                        }
                    }
                }

            });
        }
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            logger.error(e.toString());
        }
        cachedThreadPool.shutdown();
    }

    /**
     * 测试parsePackageInfo()方法
     */
    void testParsePackageInfo() {
        String url = "https://f-droid.org/packages/";
        HttpResponse response = HttpUtils.getRawHtml(url);
        // 获取响应状态码
        int StatusCode = response.getStatusLine().getStatusCode();
        // 如果状态响应码为200，则获取html实体内容或者json文件
        try {
            if (StatusCode == 200) {
                if (response.getEntity() == null) {

                }
                String entity;
                entity = EntityUtils.toString(response.getEntity(), "utf-8");
                new FDroidParser().parsePackageInfo(url, entity);
                // System.out.println(entity);
                EntityUtils.consume(response.getEntity());
            }
            else {
                // 否则，消耗掉实体
                EntityUtils.consume(response.getEntity());
            }
        }
        catch (ParseException pe) {
            logger.error(pe.toString());
        }
        catch (IOException e) {
            logger.error(e.toString());
        }
    }

    public static void main(String[] args) throws ParseException, IOException {
        // 测试
        // String baseUrl = "https://f-droid.org/packages/";
        // String api = "https://f-droid.org/js/index.json";
        // FDroidParser fdParse = new FDroidParser(baseUrl);
        // fdParse.parseAllAppIssuesUrl(api);
        // fdParse.getIssuesAndCommentsOfAllApp();

        // new FDroidParser().testParsePackageInfo();
        // new FDroidParser().getAppTotalPages();
    }

}
