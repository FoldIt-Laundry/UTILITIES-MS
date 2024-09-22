package com.foldit.utilites.user.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "UserDetails")
public class UserDetails {

    private String id;
    private String userName;
    private String mobileNumber;
    private UserRole userRole;
    private String checkOutOtp;
    private String fcmToken;
    private List<UserLocation> locations;

    public UserDetails() {
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getCheckOutOtp() {
        return checkOutOtp;
    }

    public void setCheckOutOtp(String checkOutOtp) {
        this.checkOutOtp = checkOutOtp;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public UserDetails(List<UserLocation> locations) {
        this.locations = locations;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public List<UserLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<UserLocation> locations) {
        this.locations = locations;
    }

}