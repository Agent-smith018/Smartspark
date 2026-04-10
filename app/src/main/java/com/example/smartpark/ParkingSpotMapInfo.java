package com.example.smartpark;

public class ParkingSpotMapInfo {
    public final String spotId;
    public final String name;
    public final String status;
    public final String lastUpdatedTime;
    public final String userId;
    public final double latitude;
    public final double longitude;
    public float distance = -1;

        public final String type; // free, paid, street, private

        public ParkingSpotMapInfo(String spotId, String name, String status, String lastUpdatedTime, String userId, double latitude, double longitude, String type) {
        this.spotId = spotId;
        this.name = name;
        this.status = status;
        this.lastUpdatedTime = lastUpdatedTime;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
            this.type = type != null ? type : "free";
        }

        // Backward compatibility constructor
        public ParkingSpotMapInfo(String spotId, String name, String status, String lastUpdatedTime, String userId, double latitude, double longitude) {
            this(spotId, name, status, lastUpdatedTime, userId, latitude, longitude, "free");
    }
}
