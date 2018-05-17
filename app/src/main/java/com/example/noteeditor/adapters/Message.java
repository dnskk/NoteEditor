package com.example.noteeditor.adapters;

public class Message {
    public boolean isMine;
    public String uid;
    public String message;

    public Message(String uid, String message, boolean isMine) {
        this.uid = uid;
        this.message = message;
        this.isMine = isMine;
    }
}
