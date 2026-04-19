package com.example.smartpark;

public class ParkingRequest {
    private String id;
    private String spotId;
    private String spotName;
    private String driverId;
    private String ownerId;
    private String status;
    private String timeString;

    public ParkingRequest(String id, String spotId, String spotName, String driverId, String ownerId, String status, String timeString) {
        this.id = id;
        this.spotId = spotId;
        this.spotName = spotName;
        this.driverId = driverId;
        this.ownerId = ownerId;
        this.status = status;
        this.timeString = timeString;
    }

    public String getId() { return id; }
    public String getSpotId() { return spotId; }
    public String getSpotName() { return spotName; }
    public String getDriverId() { return driverId; }
    public String getOwnerId() { return ownerId; }
    public String getStatus() { return status; }
    public String getTimeString() { return timeString; }
}
