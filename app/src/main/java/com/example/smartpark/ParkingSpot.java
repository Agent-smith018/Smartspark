package com.example.smartpark;

public class ParkingSpot {
    private String id;
    private String name;
    private String status;   // Available, Occupied, Reserved
    private String type;     // Standard, Accessible, EV, Motorcycle
    private String pricing;  // Free, Paid
    private String openTime;
    private String closeTime;
    private int capacity;
    private String description;
    private String lotId;

    public ParkingSpot() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public String getPricing() { return pricing; }
    public String getOpenTime() { return openTime; }
    public String getCloseTime() { return closeTime; }
    public int getCapacity() { return capacity; }
    public String getDescription() { return description; }
    public String getLotId() { return lotId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
    public void setType(String type) { this.type = type; }
    public void setPricing(String pricing) { this.pricing = pricing; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setDescription(String description) { this.description = description; }
    public void setLotId(String lotId) { this.lotId = lotId; }
}
