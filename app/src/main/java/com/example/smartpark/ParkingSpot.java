package com.example.smartpark;

import java.io.Serializable;

public class ParkingSpot implements Serializable {
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
    private String address;
    private String price;
    private String workingHours;
    private double latitude;
    private double longitude;

    public ParkingSpot() {}

    public ParkingSpot(String id, String name, String address, String price, String status) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.price = price;
        this.status = status;
    }

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
    public String getAddress() { return address; }
    public String getPrice() { return price; }
    public String getWorkingHours() { return workingHours; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

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
    public void setAddress(String address) { this.address = address; }
    public void setPrice(String price) { this.price = price; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
