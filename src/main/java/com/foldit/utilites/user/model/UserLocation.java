package com.foldit.utilites.user.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "UserDetails")
public class UserLocation {

    @Id
    private String id = String.valueOf(ObjectId.get());
    private String flatNumber;
    private String landMark;
    private String googleSuggestedAddress;
    private Double latitude;
    private Double longitude;
    private String addressType;
    private Double distanceFromNearestStore;
    private Double deliveryFeeIfApplicable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public String getLandMark() {
        return landMark;
    }

    public void setLandMark(String landMark) {
        this.landMark = landMark;
    }

    public String getGoogleSuggestedAddress() {
        return googleSuggestedAddress;
    }

    public void setGoogleSuggestedAddress(String googleSuggestedAddress) {
        this.googleSuggestedAddress = googleSuggestedAddress;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public Double getDistanceFromNearestStore() {
        return distanceFromNearestStore;
    }

    public void setDistanceFromNearestStore(Double distanceFromNearestStore) {
        this.distanceFromNearestStore = distanceFromNearestStore;
    }

    public Double getDeliveryFeeIfApplicable() {
        return deliveryFeeIfApplicable;
    }

    public void setDeliveryFeeIfApplicable(Double deliveryFeeIfApplicable) {
        this.deliveryFeeIfApplicable = deliveryFeeIfApplicable;
    }
}
