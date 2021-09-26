package com.cykka.partner;

public class Subscriber {
    private String userId, name,status, imgUri;

    public Subscriber(){

    }


    public Subscriber(String id, String userName, String status, String userImgUri){
        this.userId = userId;
        this.name = name;
        this.status = status;
        this.imgUri = imgUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
