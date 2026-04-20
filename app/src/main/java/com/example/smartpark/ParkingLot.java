package com.example.smartpark;

public class ParkingLot {
    private String id;
    private String name;
    private String address;
    private int totalSpots;
    private int availableSpots;
    private String spotType;
    private String status;
    private String pricing;
    private String openTime;
    private String closeTime;
    private String ownerId;
    private double rating;
    private int reviewCount;
    private boolean suspended;

    public ParkingLot() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public int getTotalSpots() { return totalSpots; }
    public int getAvailableSpots() { return availableSpots; }
    public String getSpotType() { return spotType; }
    public String getStatus() { return status; }
    public String getPricing() { return pricing; }
    public String getOpenTime() { return openTime; }
    public String getCloseTime() { return closeTime; }
    public String getOwnerId() { return ownerId; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public boolean isSuspended() { return suspended; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setTotalSpots(int totalSpots) { this.totalSpots = totalSpots; }
    public void setAvailableSpots(int availableSpots) { this.availableSpots = availableSpots; }
    public void setSpotType(String spotType) { this.spotType = spotType; }
    public void setStatus(String status) { this.status = status; }
    public void setPricing(String pricing) { this.pricing = pricing; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setSuspended(boolean suspended) { this.suspended = suspended; }
}
