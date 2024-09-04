package com.foldit.utilites.user.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "UserDetails")
public class UserLocationShortInfo {
    private String id;
    private String locationName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
