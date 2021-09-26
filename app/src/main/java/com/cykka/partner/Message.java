package com.cykka.partner;

public class Message {
    private String senderId, receiverId, msg, timestamp;
    private int type, id;

    public Message() {
    }

    public Message(String senderId, String receiverId, String msg, String timestamp, int type, int id) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.msg = msg;
        this.timestamp = timestamp;
        this.type = type;
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String name) {
        this.senderId = name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
