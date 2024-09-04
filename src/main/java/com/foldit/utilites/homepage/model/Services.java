package com.foldit.utilites.homepage.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ServiceOffered")
public class Services {
    private String typesOfService;
    private String photoId;
    private String description;
    private Double ratePerKg;

    public Services(String typesOfService, String photoId, String description, Double ratePerKg) {
        this.typesOfService = typesOfService;
        this.photoId = photoId;
        this.description = description;
        this.ratePerKg = ratePerKg;
    }

    public String getTypesOfService() {
        return typesOfService;
    }

    public void setTypesOfService(String typesOfService) {
        this.typesOfService = typesOfService;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRatePerKg() {
        return ratePerKg;
    }

    public void setRatePerKg(Double ratePerKg) {
        this.ratePerKg = ratePerKg;
    }
}
