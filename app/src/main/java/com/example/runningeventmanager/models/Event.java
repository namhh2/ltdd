package com.example.runningeventmanager.models;

public class Event {
    private long id;
    private String name;
    private String description;
    private String date;
    private String location;
    private double distance; // in kilometers
    private String paceRequirement;
    private int maxParticipants;
    private long createdBy; // User ID of the creator
    private String status; // UPCOMING, ONGOING, COMPLETED, CANCELLED
    private String createdAt;
    private String updatedAt;
    
    // Default constructor
    public Event() {
    }
    
    // Constructor for creating a new event
    public Event(String name, String description, String date, String location, 
                double distance, String paceRequirement, int maxParticipants, long createdBy) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.location = location;
        this.distance = distance;
        this.paceRequirement = paceRequirement;
        this.maxParticipants = maxParticipants;
        this.createdBy = createdBy;
        this.status = "UPCOMING"; // Default status
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public String getPaceRequirement() {
        return paceRequirement;
    }
    
    public void setPaceRequirement(String paceRequirement) {
        this.paceRequirement = paceRequirement;
    }
    
    public int getMaxParticipants() {
        return maxParticipants;
    }
    
    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
    
    public long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isUpcoming() {
        return "UPCOMING".equals(status);
    }
    
    public boolean isOngoing() {
        return "ONGOING".equals(status);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", location='" + location + '\'' +
                ", distance=" + distance +
                ", status='" + status + '\'' +
                '}';
    }
} 