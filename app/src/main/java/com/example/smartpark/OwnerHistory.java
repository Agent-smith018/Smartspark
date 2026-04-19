package com.example.smartpark;

public class OwnerHistory {
    private String id;
    private String spotName;
    private String status;
    private String timeString;

    public OwnerHistory(String id, String spotName, String status, String timeString) {
        this.id = id;
        this.spotName = spotName;
        this.status = status;
        this.timeString = timeString;
    }

    public String getId() { return id; }
    public String getSpotName() { return spotName; }
    public String getStatus() { return status; }
    public String getTimeString() { return timeString; }
}
