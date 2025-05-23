package com.foldit.utilites.homepage.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ServiceOffered")
public class Services {

    private String type;
    private String serviceName;
    private String serviceId;
    private String description;
    private double pricing;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPricing() {
        return pricing;
    }

    public void setPricing(double pricing) {
        this.pricing = pricing;
    }
}
