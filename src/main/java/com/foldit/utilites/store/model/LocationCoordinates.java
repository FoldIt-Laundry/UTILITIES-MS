package com.foldit.utilites.store.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "StoreInformation")
public class LocationCoordinates {
    private String type;
    private List<Double> coordinates ;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
