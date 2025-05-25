package com.example.runningeventmanager.models;

import java.util.List;

public class StravaData {
    private long id;
    private long userId;
    private String activityId;
    private String name;
    private String type;  // Run, Ride, etc.
    private String startDate;
    private double distance;  // in meters
    private int movingTime;   // in seconds
    private int elapsedTime;  // in seconds
    private double elevationGain;
    private double averageSpeed;  // meters per second
    private double maxSpeed;      // meters per second
    private double averageHeartrate;
    private double maxHeartrate;
    private List<Split> splits;
    private String createdAt;
    
    // Default constructor
    public StravaData() {
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
    
    public String getActivityId() {
        return activityId;
    }
    
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public int getMovingTime() {
        return movingTime;
    }
    
    public void setMovingTime(int movingTime) {
        this.movingTime = movingTime;
    }
    
    public int getElapsedTime() {
        return elapsedTime;
    }
    
    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    
    public double getElevationGain() {
        return elevationGain;
    }
    
    public void setElevationGain(double elevationGain) {
        this.elevationGain = elevationGain;
    }
    
    public double getAverageSpeed() {
        return averageSpeed;
    }
    
    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
    
    public double getMaxSpeed() {
        return maxSpeed;
    }
    
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    
    public double getAverageHeartrate() {
        return averageHeartrate;
    }
    
    public void setAverageHeartrate(double averageHeartrate) {
        this.averageHeartrate = averageHeartrate;
    }
    
    public double getMaxHeartrate() {
        return maxHeartrate;
    }
    
    public void setMaxHeartrate(double maxHeartrate) {
        this.maxHeartrate = maxHeartrate;
    }
    
    public List<Split> getSplits() {
        return splits;
    }
    
    public void setSplits(List<Split> splits) {
        this.splits = splits;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public double getDistanceInKilometers() {
        return distance / 1000.0;
    }
    
    public String getFormattedMovingTime() {
        int hours = movingTime / 3600;
        int minutes = (movingTime % 3600) / 60;
        int seconds = movingTime % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    public String getFormattedPace() {
        // Calculate pace as minutes per kilometer
        double paceSeconds = movingTime / (distance / 1000.0);
        int paceMinutes = (int) (paceSeconds / 60);
        int paceSecondsRemainder = (int) (paceSeconds % 60);
        return String.format("%d:%02d", paceMinutes, paceSecondsRemainder);
    }
    
    // Inner class for splits data
    public static class Split {
        private int splitNumber;
        private double distance;  // in meters
        private int movingTime;   // in seconds
        private double elevationDifference;
        private double averageSpeed;  // meters per second
        
        public Split() {
        }
        
        public int getSplitNumber() {
            return splitNumber;
        }
        
        public void setSplitNumber(int splitNumber) {
            this.splitNumber = splitNumber;
        }
        
        public double getDistance() {
            return distance;
        }
        
        public void setDistance(double distance) {
            this.distance = distance;
        }
        
        public int getMovingTime() {
            return movingTime;
        }
        
        public void setMovingTime(int movingTime) {
            this.movingTime = movingTime;
        }
        
        public double getElevationDifference() {
            return elevationDifference;
        }
        
        public void setElevationDifference(double elevationDifference) {
            this.elevationDifference = elevationDifference;
        }
        
        public double getAverageSpeed() {
            return averageSpeed;
        }
        
        public void setAverageSpeed(double averageSpeed) {
            this.averageSpeed = averageSpeed;
        }
        
        public String getFormattedPace() {
            // Calculate pace as minutes per kilometer
            double paceSeconds = movingTime / (distance / 1000.0);
            int paceMinutes = (int) (paceSeconds / 60);
            int paceSecondsRemainder = (int) (paceSeconds % 60);
            return String.format("%d:%02d", paceMinutes, paceSecondsRemainder);
        }
    }
} 