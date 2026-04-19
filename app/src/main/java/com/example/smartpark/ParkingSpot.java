package com.example.smartpark;

import java.io.Serializable;

public class ParkingSpot implements Serializable {
    private String id;
    private String name;
    private String address;
    private String price;
    private String status;
    private String description;
    private String workingHours;
    private String type;
    private int capacity;
    private double latitude;
    private double longitude;

    // Required for Firebase
    public ParkingSpot() {
    }

    public ParkingSpot(String id, String name, String address, String price, String status) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.price = price;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPrice() { return price; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getWorkingHours() { return workingHours; }
    public String getType() { return type; }
    public int getCapacity() { return capacity; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setPrice(String price) { this.price = price; }
    public void setStatus(String status) { this.status = status; }
    public void setDescription(String description) { this.description = description; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public void setType(String type) { this.type = type; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}