package edu.ecnu.web.crawler.model;

public class DroidIssueDto {
    private int id;

    private String title;

    private String body;

    private String comments_url;

    private int comments;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getComments_url() {
        return comments_url;
    }

    public void setComments_url(String comments_url) {
        this.comments_url = comments_url;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public String toString() {
        return "{id=" + id + ", title=" + title + ", body=" + body + ", comments_url=" + comments_url + ", comments="
            + comments + "}";
    }
}
