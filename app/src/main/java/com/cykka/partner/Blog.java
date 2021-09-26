package com.cykka.partner;

public class Blog {
    private String title, description, link, url, id;

    public Blog(){
    }

    public Blog(String title, String description, String link, String url, String id){
        this.title = title;
        this.description = description;
        this.link = link;
        this.url = url;
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
