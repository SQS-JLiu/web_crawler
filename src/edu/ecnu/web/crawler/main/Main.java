package edu.ecnu.web.crawler.main;

import edu.ecnu.web.crawler.parser.FDroidParser;

public class Main {

    public static void main(String[] args) {
        String baseUrl = "https://f-droid.org/packages/";
        FDroidParser fdParse = new FDroidParser(baseUrl);
        // 执行时先建数据库表，最好保证表中数据为空，建表见FDroidDB.sql文件
        // 获取所有app并入库
        fdParse.parseAllAppIssuesUrl("https://f-droid.org/js/index.json");
        // 获取所有issues并入库
        fdParse.getIssuesOfAllApp();
        // 获取所有comments并入库
        fdParse.getCommentsOfAllIssue();
    }

}
