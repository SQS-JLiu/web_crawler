package edu.ecnu.web.crawler.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

    private final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * GET方式请求一个HTML网页
     * 
     * @param url
     * @return
     */
    public static HttpResponse getRawHtml(String url, String user, String passwd) {
        // 获取响应文件，即html，采用get方法获取响应数据
        HttpGet httpGet = new HttpGet(url);
        // setConnectTimeout设置连接的超时时间
        // setConnectionRequestTimeout设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
        // setSocketTimeout设置响应超时时间,单位毫秒
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(5000)
            .setSocketTimeout(20000).build();
        httpGet.setConfig(requestConfig);
        if (user != null && passwd != null) {
            httpGet.addHeader(getHeader(user, passwd));
        }
        // new一个HttpResponse对象，并初始化
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 0, "OK");
        HttpClient client = HttpClients.createDefault();
        try {
            // 执行get方法
            response = client.execute(httpGet);
        }
        catch (IOException e) {
            System.out.println(e.toString());
            logger.error("getRawHtml --> " + e.toString());
        }
        return response;
    }

    public static HttpResponse getRawHtml(String url) {
        return getRawHtml(url, null, null);
    }

    private static BasicHeader getHeader(String user, String passwd) {
        String auth = user + ":" + passwd;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        return new BasicHeader("Authorization", authHeader);
    }

    /**
     * POST方式请求一个HTML网页
     * 
     * @param url
     * @param paramList
     * @return
     */
    public static HttpResponse postRawHtml(String url, List<NameValuePair> paramList) {
        HttpPost httpPsot = new HttpPost(url);// 创建HttpPost对象
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(1000)
            .setSocketTimeout(5000).build();
        httpPsot.setConfig(requestConfig);
        HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        try {
            if (paramList == null) {
                paramList = new ArrayList<NameValuePair>();
            }
            httpPsot.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
            HttpClient client = HttpClients.createDefault();
            httpResponse = client.execute(httpPsot);
        }
        catch (ClientProtocolException e) {
            logger.error(e.toString());
        }
        catch (IOException e) {
            logger.error(e.toString());
        }
        return httpResponse;
    }

    public static HttpResponse postRawHtml(String url) {
        return postRawHtml(url, null);
    }

    public static String sendGetRequest(String getUrl) {
        StringBuffer sb = new StringBuffer();
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            URL url = new URL(getUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setAllowUserInteraction(false);
            isr = new InputStreamReader(url.openStream());
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void main(String[] args) throws UnsupportedOperationException, IOException {
        // 测试
        // String url = "https://f-droid.org/packages";
        String url2 = "https://api.github.com/repos/HugoGresse/Anecdote/issues?state=all&page=1";
        HttpResponse response = HttpUtils.getRawHtml(url2, "664838289", "lj664838289");
        // 获取响应状态码
        int StatusCode = response.getStatusLine().getStatusCode();
        // 如果状态响应码为200，则获取html实体内容或者json文件
        if (StatusCode == 200) {
            String entity = EntityUtils.toString(response.getEntity(), "utf-8");
            System.out.println(response.getHeaders("Link") + "\t" + entity);
            for (Header s : response.getHeaders("Link")) {
                int value = Integer.valueOf(s.getValue().split("&page=")[2].split(">")[0]);
                System.out.println(s.getName() + ": " + value + "\n");
                System.out.println(s.getValue());
            }
            EntityUtils.consume(response.getEntity());
        }
        else {
            // 否则，消耗掉实体
            System.out.println(StatusCode);
            EntityUtils.consume(response.getEntity());
        }
        // System.out.println(HttpUtils.sendGetRequest("http://f-droid.org/js/index.json"));
    }

}
