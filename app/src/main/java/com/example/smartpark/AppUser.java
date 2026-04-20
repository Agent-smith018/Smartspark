package com.example.smartpark;

public class AppUser {
    private String id;
    private String name;
    private String email;
    private String role;      // driver, owner, admin
    private boolean approved;
    private boolean rejected;
    private boolean suspended;
    private String createdAt;
    private String adminNote;

    public AppUser() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isApproved() { return approved; }
    public boolean isRejected() { return rejected; }
    public boolean isSuspended() { return suspended; }
    public String getCreatedAt() { return createdAt; }
    public String getAdminNote() { return adminNote; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public void setRejected(boolean rejected) { this.rejected = rejected; }
    public void setSuspended(boolean suspended) { this.suspended = suspended; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}
