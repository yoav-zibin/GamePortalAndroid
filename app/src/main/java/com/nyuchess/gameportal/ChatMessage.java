package com.nyuchess.gameportal;

import com.google.firebase.database.ServerValue;

import java.util.Date;

/**
 * Created by Victor on 10/4/2017.
 */

public class ChatMessage {

    private String senderUid;
    private String message;
    private long timestamp;

    public ChatMessage(String messageText, String messageUser) {
        this.senderUid = messageText;
        this.message = messageUser;

        // Initialize to current time
        timestamp = new Date().getTime();
    }

    public ChatMessage(){

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}