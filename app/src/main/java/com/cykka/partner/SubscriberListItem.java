package com.cykka.partner;

public class SubscriberListItem {
    private String userId, name, imgUri, lastMsg, timeStamp;

    public SubscriberListItem(){}

    public SubscriberListItem(String userId, String name, String imgUri, String lastMsg, String timeStamp ){
        this.userId = userId;
        this.name = name;
        this.imgUri = imgUri;
        this.lastMsg = lastMsg;
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
