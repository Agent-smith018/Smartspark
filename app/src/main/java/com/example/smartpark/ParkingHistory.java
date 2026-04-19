package com.example.smartpark;

public class ParkingHistory {
    public String id;
    public String spotId;
    public String spotName;
    public double latitude;
    public double longitude;
    public String parkedAtText;

    public ParkingHistory(String id, String spotId, String spotName, double latitude, double longitude, String parkedAtText) {
        this.id = id;
        this.spotId = spotId;
        this.spotName = spotName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.parkedAtText = parkedAtText;
    }
}
