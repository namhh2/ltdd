package com.example.runningeventmanager.models;

public class Achievement {
    private long id;
    private long userId;
    private long eventId;
    private String title;
    private String description;
    private String date;
    private String createdAt;
    private String updatedAt;
    
    // Additional fields that aren't stored in DB but useful for display
    private String userName;
    private String eventName;
    
    // Default constructor
    public Achievement() {
    }
    
    // Constructor for creating a new achievement
    public Achievement(long userId, long eventId, String title, String description, String date) {
        this.userId = userId;
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.date = date;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public long getEventId() {
        return eventId;
    }
    
    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    @Override
    public String toString() {
        return "Achievement{" +
                "id=" + id +
                ", userId=" + userId +
                ", eventId=" + eventId +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
} 