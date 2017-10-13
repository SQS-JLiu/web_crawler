package edu.ecnu.web.crawler.model;

public class DroidAppDto {
    private int id;

    private String appName;

    private String packageName;

    private String describe;

    private String appUrl;

    private String issuesUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIssuesUrl() {
        return issuesUrl;
    }

    public void setIssuesUrl(String issuesUrl) {
        this.issuesUrl = issuesUrl;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public String toString() {
        return "{id=" + id + ", appName=" + appName + ", packageName=" + packageName + ", describe=" + describe
            + ", appUrl=" + appUrl + ", issuesUrl=" + issuesUrl + "}";
    }
}
