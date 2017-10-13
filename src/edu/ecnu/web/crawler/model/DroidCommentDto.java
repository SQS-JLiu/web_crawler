package edu.ecnu.web.crawler.model;

public class DroidCommentDto {
    private String comments_url;

    private String body;

    public String getComments_url() {
        return comments_url;
    }

    public void setComments_url(String comments_url) {
        this.comments_url = comments_url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String toStirng() {
        return "{comments_url=" + comments_url + ", body=" + body + "}";
    }
}
