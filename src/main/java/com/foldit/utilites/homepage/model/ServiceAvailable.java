package com.foldit.utilites.homepage.model;

import java.util.List;

public class ServiceAvailable {
    private  boolean validated;
    private List<Services> servicesList;

    public ServiceAvailable() {
    }

    public ServiceAvailable(boolean validated, List<Services> servicesList) {
        this.validated = validated;
        this.servicesList = servicesList;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public List<Services> getServicesList() {
        return servicesList;
    }

    public void setServicesList(List<Services> servicesList) {
        this.servicesList = servicesList;
    }
}
