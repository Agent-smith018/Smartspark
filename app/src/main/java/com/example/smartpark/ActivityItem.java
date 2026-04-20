package com.example.smartpark;

import com.google.firebase.Timestamp;

public class ActivityItem {
    private String message;
    private String subMessage;
    private Timestamp timestamp;

    public ActivityItem() {}

    public String getMessage() { return message; }
    public String getSubMessage() { return subMessage; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setMessage(String message) { this.message = message; }
    public void setSubMessage(String subMessage) { this.subMessage = subMessage; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
