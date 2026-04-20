package com.example.smartpark;

import com.google.firebase.Timestamp;

public class Report {
    private String id;
    private String title;
    private String description;
    private String type;       // user, spot
    private String targetId;   // userId or spotId
    private String targetName;
    private boolean critical;
    private boolean resolved;
    private Timestamp timestamp;

    public Report() {}

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getTargetId() { return targetId; }
    public String getTargetName() { return targetName; }
    public boolean isCritical() { return critical; }
    public boolean isResolved() { return resolved; }
    public Timestamp getTimestamp() { return timestamp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public void setCritical(boolean critical) { this.critical = critical; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
