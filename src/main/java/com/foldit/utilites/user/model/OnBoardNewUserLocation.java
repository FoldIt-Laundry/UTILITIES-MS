package com.foldit.utilites.user.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "UserDetails")
public class OnBoardNewUserLocation {
    private UserLocation userLocation;
    private String userId;
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
