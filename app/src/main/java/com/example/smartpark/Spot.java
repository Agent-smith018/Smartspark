package com.example.smartpark;

public class Spot {
    private String id;
    private String name;
    private String address;
    private int totalSpots;
    private String spotType;
    private String status; // "Open" or "Closed"
    private String pricingType; // "Free" or "Paid"
    private double hourlyRate;
    private String openTime;
    private String closeTime;
    private String description;

    public Spot(String id, String name, String address, int totalSpots, String spotType,
                String status, String pricingType, double hourlyRate, String openTime,
                String closeTime, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.totalSpots = totalSpots;
        this.spotType = spotType;
        this.status = status;
        this.pricingType = pricingType;
        this.hourlyRate = hourlyRate;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.description = description;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getTotalSpots() { return totalSpots; }
    public void setTotalSpots(int totalSpots) { this.totalSpots = totalSpots; }
    public String getSpotType() { return spotType; }
    public void setSpotType(String spotType) { this.spotType = spotType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPricingType() { return pricingType; }
    public void setPricingType(String pricingType) { this.pricingType = pricingType; }
    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
