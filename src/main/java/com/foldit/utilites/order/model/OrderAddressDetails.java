package com.foldit.utilites.order.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "OrderDetails")
public class OrderAddressDetails {
    private String addressName;
    private String completeAddress;

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getCompleteAddress() {
        return completeAddress;
    }

    public void setCompleteAddress(String completeAddress) {
        this.completeAddress = completeAddress;
    }
}
