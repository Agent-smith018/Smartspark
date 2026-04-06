package com.example.smartpark;

import java.io.Serializable;

public class ParkingSpot implements Serializable {
    private String id;
    private String name;
    private String address;
    private String price;

    // Required for Firebase
    public ParkingSpot() {
    }

    public ParkingSpot(String id, String name, String address, String price) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPrice() { return price; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setPrice(String price) { this.price = price; }
}