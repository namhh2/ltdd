package com.example.runningeventmanager.models;

public class Registration {
    private long id;
    private long userId;
    private long eventId;
    private String status; // REGISTERED, CONFIRMED, COMPLETED, CANCELLED
    private String bibNumber;
    private String finishTime;
    private String pace;
    private String createdAt;
    private String updatedAt;
    
    // Additional fields that aren't stored in DB but useful for display
    private String userName;
    private String eventName;
    
    // Default constructor
    public Registration() {
    }
    
    // Constructor for creating a new registration
    public Registration(long userId, long eventId) {
        this.userId = userId;
        this.eventId = eventId;
        this.status = "REGISTERED"; // Default status
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getBibNumber() {
        return bibNumber;
    }
    
    public void setBibNumber(String bibNumber) {
        this.bibNumber = bibNumber;
    }
    
    public String getFinishTime() {
        return finishTime;
    }
    
    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }
    
    public String getPace() {
        return pace;
    }
    
    public void setPace(String pace) {
        this.pace = pace;
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
    
    public boolean isRegistered() {
        return "REGISTERED".equals(status);
    }
    
    public boolean isConfirmed() {
        return "CONFIRMED".equals(status);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
    
    @Override
    public String toString() {
        return "Registration{" +
                "id=" + id +
                ", userId=" + userId +
                ", eventId=" + eventId +
                ", status='" + status + '\'' +
                ", bibNumber='" + bibNumber + '\'' +
                ", finishTime='" + finishTime + '\'' +
                '}';
    }
} 