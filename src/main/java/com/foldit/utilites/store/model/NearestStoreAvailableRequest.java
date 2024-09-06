package com.foldit.utilites.store.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class NearestStoreAvailableRequest {
    private Double xCordinates;
    private Double yCordinates;

    public Double getxCordinates() {
        return xCordinates;
    }

    public void setxCordinates(Double xCordinates) {
        this.xCordinates = xCordinates;
    }

    public Double getyCordinates() {
        return yCordinates;
    }

    public void setyCordinates(Double yCordinates) {
        this.yCordinates = yCordinates;
    }
}
