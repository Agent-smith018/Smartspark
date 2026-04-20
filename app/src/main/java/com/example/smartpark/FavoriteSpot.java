package com.example.smartpark;

public class FavoriteSpot {
    public String id;
    public String spotId;
    public String spotName;
    public String spotStatus;
    public String ownerId;
    public double latitude;
    public double longitude;
    public String savedAtText;

    public FavoriteSpot(String id, String spotId, String spotName, String spotStatus, String ownerId, double latitude, double longitude, String savedAtText) {
        this.id = id;
        this.spotId = spotId;
        this.spotName = spotName;
        this.spotStatus = spotStatus;
        this.ownerId = ownerId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.savedAtText = savedAtText;
    }
}
