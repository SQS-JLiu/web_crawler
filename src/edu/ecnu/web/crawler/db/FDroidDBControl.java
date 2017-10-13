package edu.ecnu.web.crawler.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ecnu.web.crawler.model.DroidAppDto;
import edu.ecnu.web.crawler.model.DroidCommentDto;
import edu.ecnu.web.crawler.model.DroidIssueDto;
import edu.ecnu.web.crawler.utils.MySqlUtils;

public class FDroidDBControl {
    private final static Logger logger = LoggerFactory.getLogger(FDroidDBControl.class);

    public static DroidAppDto queryAppById(int id) {
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        String sql = "select * from app where id =?";
        DroidAppDto appModel = null;
        try {
            resultSet = dbc.execSql(sql, id);
            if (resultSet.next()) {
                appModel = new DroidAppDto();
                appModel.setId(id);
                appModel.setAppName(resultSet.getString("name"));
                appModel.setPackageName(resultSet.getString("packageName"));
                appModel.setDescribe(resultSet.getString("introduction"));
                appModel.setAppUrl(resultSet.getString("appUrl"));
                appModel.setIssuesUrl(resultSet.getString("issuesUrl"));
            }
        }
        catch (SQLException e) {
            logger.error(e.toString());
            e.printStackTrace();
        }
        finally {
            colseConn(dbc, resultSet);
        }
        return appModel;
    }

    public static LinkedBlockingQueue<DroidAppDto> queryAllApp() {
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        String sql = "select * from app";
        DroidAppDto appModel = null;
        LinkedBlockingQueue<DroidAppDto> appModelQueue = new LinkedBlockingQueue<DroidAppDto>();
        try {
            resultSet = dbc.execSql(sql);
            while (resultSet.next()) {
                appModel = new DroidAppDto();
                appModel.setId(resultSet.getInt("id"));
                appModel.setAppName(resultSet.getString("name"));
                appModel.setPackageName(resultSet.getString("packageName"));
                appModel.setDescribe(resultSet.getString("introduction"));
                appModel.setAppUrl(resultSet.getString("appUrl"));
                appModel.setIssuesUrl(resultSet.getString("issuesUrl"));
                appModelQueue.add(appModel);
            }
        }
        catch (SQLException e) {
            logger.error(e.toString());
            e.printStackTrace();
        }
        finally {
            colseConn(dbc, resultSet);
        }
        return appModelQueue;
    }

    public static boolean insertAppInfo(DroidAppDto appModel) {
        if (queryAppById(appModel.getId()) != null) {
            return true;
        }
        String sql;
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        try {
            if (appModel.getIssuesUrl() == null) {
                sql = "insert into app(id,name,packageName,introduction,appUrl) values(?,?,?,?,?)";
                resultSet = dbc.execSql(sql, appModel.getId(), appModel.getAppName(), appModel.getPackageName(),
                    appModel.getDescribe(), appModel.getAppUrl());
            }
            else {
                sql = "insert into app(id,name,packageName,introduction,appUrl,issuesUrl) values(?,?,?,?,?,?)";
                resultSet = dbc.execSql(sql, appModel.getId(), appModel.getAppName(), appModel.getPackageName(),
                    appModel.getDescribe(), appModel.getAppUrl(), appModel.getIssuesUrl());
            }
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
        finally {
            colseConn(dbc, resultSet);
        }
    }

    public static boolean deleteAppInfoById(int id) {
        String sql = "delete from app where id=?";
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        try {
            resultSet = dbc.execSql(sql, id);
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
            return false;
        }
        finally {
            colseConn(dbc, resultSet);
        }
    }

    public static LinkedBlockingQueue<DroidIssueDto> queryAllIssues() {
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        String sql = "select * from issues";
        DroidIssueDto issue = null;
        LinkedBlockingQueue<DroidIssueDto> issuesQueue = new LinkedBlockingQueue<DroidIssueDto>();
        try {
            resultSet = dbc.execSql(sql);
            while (resultSet.next()) {
                issue = new DroidIssueDto();
                issue.setId(resultSet.getInt("id"));
                issue.setTitle(resultSet.getString("title"));
                issue.setBody(resultSet.getString("body"));
                issue.setComments_url(resultSet.getString("comments_url"));
                issue.setComments(resultSet.getInt("comments"));
                issuesQueue.add(issue);
            }
        }
        catch (SQLException e) {
            logger.error(e.toString());
            e.printStackTrace();
        }
        finally {
            colseConn(dbc, resultSet);
        }
        return issuesQueue;
    }

    public static DroidIssueDto queryIssues(int id, String title) {
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        String sql = "select * from issues where id =? and title =?";
        DroidIssueDto issue = null;
        try {
            resultSet = dbc.execSql(sql, id, title);
            if (resultSet.next()) {
                issue = new DroidIssueDto();
                issue.setId(id);
                issue.setTitle(resultSet.getString("title"));
                issue.setBody(resultSet.getString("body"));
                issue.setComments_url(resultSet.getString("comments_url"));
                issue.setComments(resultSet.getInt("comments"));
            }
        }
        catch (SQLException e) {
            logger.error(e.toString());
            e.printStackTrace();
        }
        finally {
            colseConn(dbc, resultSet);
        }
        return issue;
    }

    public static boolean insertIssues(DroidIssueDto issue) {
        String sql = "insert into issues(id,title,body,comments_url,comments) values(?,?,?,?,?)";
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        try {
            resultSet = dbc.execSql(sql, issue.getId(), issue.getTitle(), issue.getBody(), issue.getComments_url(),
                issue.getComments());
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
        finally {
            colseConn(dbc, resultSet);
        }
    }

    public static boolean deleteIssuesById(int id) {
        String sql = "delete from issues where id=?";
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        try {
            resultSet = dbc.execSql(sql, id);
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
            return false;
        }
        finally {
            colseConn(dbc, resultSet);
        }
    }

    public static LinkedList<DroidCommentDto> queryComments(String comments_url) {
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        String sql = "select * from comments where comments_url =?";
        DroidCommentDto comment = null;
        LinkedList<DroidCommentDto> commentsList = new LinkedList<DroidCommentDto>();
        try {
            resultSet = dbc.execSql(sql, comments_url);
            while (resultSet.next()) {
                comment = new DroidCommentDto();
                comment.setComments_url(resultSet.getString("comments_url"));
                comment.setBody(resultSet.getString("body"));
                commentsList.add(comment);
            }
        }
        catch (SQLException e) {
            logger.error(e.toString());
            e.printStackTrace();
        }
        finally {
            colseConn(dbc, resultSet);
        }
        return commentsList;
    }

    public static boolean insertComments(DroidCommentDto comment) {
        String sql = "insert into comments(comments_url,body) values(?,?)";
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        try {
            resultSet = dbc.execSql(sql, comment.getComments_url(), comment.getBody());
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
        finally {
            colseConn(dbc, resultSet);
        }
    }

    public static boolean deleteComments(String comments_url) {
        String sql = "delete from comments where comments_url=?";
        ResultSet resultSet = null;
        MySqlUtils dbc = new MySqlUtils();
        try {
            resultSet = dbc.execSql(sql, comments_url);
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
            return false;
        }
        finally {
            colseConn(dbc, resultSet);
        }
    }

    private static void colseConn(MySqlUtils dbc, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                // 关闭数据库执行
                resultSet.close();
            }
            if (dbc != null) {
                dbc.closeConnPs();
            }
        }
        catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 测试mian函数
        // DroidAppDto appModel = new DroidAppDto();
        // appModel.setId(1);
        // appModel.setAppName("appName");
        // appModel.setPackageName("packageName");
        // appModel.setDescribe("describe");
        // appModel.setAppUrl("appUrl");
        // FDroidDBControl.insertAppInfo(appModel);
        // DroidIssueDto issue = new DroidIssueDto();
        // issue.setId(1);
        // issue.setTitle("title");
        // issue.setBody("body");
        // issue.setcomments_url("comments_url");
        // issue.setComments(1);
        // FDroidDBControl.insertIssues(issue);
        // DroidCommentDto comment = new DroidCommentDto();
        // comment.setComments_url("comments_url");
        // comment.setBody("body");
        // FDroidDBControl.insertComments(comment);

        // System.out.println(FDroidDBControl.queryAppById(1).toString());
        // List<DroidIssueDto> issuesList = FDroidDBControl.queryIssuesById(1);
        // if (issuesList.size() == 1) {
        // System.out.println(issuesList.get(0).toString());
        // }
        // List<DroidCommentDto> commentsList = FDroidDBControl.queryComments("comments_url");
        // if (commentsList.size() == 1) {
        // System.out.println(commentsList.get(0).toStirng());
        // }
        // System.out.println("delete app: " + FDroidDBControl.deleteAppInfoById(1));
        // System.out.println("delete issue: " + FDroidDBControl.deleteIssuesById(1));
        // System.out.println("delete comment: " + FDroidDBControl.deleteComments("comments_url"));
    }

}
