package com.foldit.utilites.homepage.model;

import com.foldit.utilites.store.model.ServiceOffered;

import java.util.ArrayList;
import java.util.List;

public class ServiceAvailable {
    private  boolean validated;
    private List<ServiceOffered> servicesList = new ArrayList<>();

    public ServiceAvailable() {
    }

    public ServiceAvailable(boolean validated, List<ServiceOffered> servicesList) {
        this.validated = validated;
        this.servicesList = servicesList;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public List<ServiceOffered> getServicesList() {
        return servicesList;
    }

    public void setServicesList(List<ServiceOffered> servicesList) {
        this.servicesList = servicesList;
    }
}
