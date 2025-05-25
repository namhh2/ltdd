package com.example.runningeventmanager.models;

public class User {
    private long id;
    private String username;
    private String email;
    private String password; // Encrypted
    private boolean isAdmin;
    private String stravaId;
    private String stravaToken;
    private String avatar;
    private String createdAt;
    private String updatedAt;
    
    // Default constructor
    public User() {
    }
    
    // Constructor for registration
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.isAdmin = false; // Default role
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    
    public String getStravaId() {
        return stravaId;
    }
    
    public void setStravaId(String stravaId) {
        this.stravaId = stravaId;
    }
    
    public String getStravaToken() {
        return stravaToken;
    }
    
    public void setStravaToken(String stravaToken) {
        this.stravaToken = stravaToken;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
    
    public boolean isConnectedWithStrava() {
        return stravaId != null && !stravaId.isEmpty() && 
               stravaToken != null && !stravaToken.isEmpty();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                ", stravaConnected=" + isConnectedWithStrava() +
                '}';
    }
} 