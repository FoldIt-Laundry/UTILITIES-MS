package com.foldit.utilites.store.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "StoreInformation")
public class StoreDetails {

    private String id;
    private String name;
    private List<String> phoneNumber;
    private String email;
    private Double averageEtd;
    private Double deliveryFeePerKmAfterThreshold;
    private Double freeDeliveryDistanceAllowed;
    private Double rating;
    private List<RatingAndComment> ratingCommentList;
    private String description;
    private String operatingHourStartTime;
    private String operatingHourEndTime;
    private StoreLocation storeLocation;
    private List<ServiceOffered> serviceOffered;
    private List<String> shopAdminIds = new ArrayList<>();
    private List<String> shopWorkerIds = new ArrayList<>();
    private List<String> shopRiderIds = new ArrayList<>();

    public List<String> getShopRiderIds() {
        return shopRiderIds;
    }

    public void setShopRiderIds(List<String> shopRiderIds) {
        this.shopRiderIds = shopRiderIds;
    }

    public Double getDeliveryFeePerKmAfterThreshold() {
        return deliveryFeePerKmAfterThreshold;
    }

    public void setDeliveryFeePerKmAfterThreshold(Double deliveryFeePerKmAfterThreshold) {
        this.deliveryFeePerKmAfterThreshold = deliveryFeePerKmAfterThreshold;
    }

    public Double getFreeDeliveryDistanceAllowed() {
        return freeDeliveryDistanceAllowed;
    }

    public void setFreeDeliveryDistanceAllowed(Double freeDeliveryDistanceAllowed) {
        this.freeDeliveryDistanceAllowed = freeDeliveryDistanceAllowed;
    }

    public List<String> getShopAdminIds() {
        return shopAdminIds;
    }

    public void setShopAdminIds(List<String> shopAdminIds) {
        this.shopAdminIds = shopAdminIds;
    }

    public List<String> getShopWorkerIds() {
        return shopWorkerIds;
    }

    public void setShopWorkerIds(List<String> shopWorkerIds) {
        this.shopWorkerIds = shopWorkerIds;
    }

    public List<ServiceOffered> getServiceOffered() {
        return serviceOffered;
    }

    public void setServiceOffered(List<ServiceOffered> serviceOffered) {
        this.serviceOffered = serviceOffered;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StoreLocation getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(StoreLocation storeLocation) {
        this.storeLocation = storeLocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(List<String> phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getAverageEtd() {
        return averageEtd;
    }

    public void setAverageEtd(Double averageEtd) {
        this.averageEtd = averageEtd;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<RatingAndComment> getRatingCommentList() {
        return ratingCommentList;
    }

    public void setRatingCommentList(List<RatingAndComment> ratingCommentList) {
        this.ratingCommentList = ratingCommentList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperatingHourStartTime() {
        return operatingHourStartTime;
    }

    public void setOperatingHourStartTime(String operatingHourStartTime) {
        this.operatingHourStartTime = operatingHourStartTime;
    }

    public String getOperatingHourEndTime() {
        return operatingHourEndTime;
    }

    public void setOperatingHourEndTime(String operatingHourEndTime) {
        this.operatingHourEndTime = operatingHourEndTime;
    }

}
