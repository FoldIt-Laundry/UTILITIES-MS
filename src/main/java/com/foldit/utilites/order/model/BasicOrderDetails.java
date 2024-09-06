package com.foldit.utilites.order.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "OrderDetails")
public class BasicOrderDetails {
    private String id;
    private String orderDeliveryTimeStamp;
    private OrderAddressDetails userAddress;
    private OrderAddressDetails shopAddress;
    @Field("billDetails.finalPrice")
    private Double finalPrice;
    private List<Services> addedService;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderDeliveryTimeStamp() {
        return orderDeliveryTimeStamp;
    }

    public void setOrderDeliveryTimeStamp(String orderDeliveryTimeStamp) {
        this.orderDeliveryTimeStamp = orderDeliveryTimeStamp;
    }

    public OrderAddressDetails getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(OrderAddressDetails userAddress) {
        this.userAddress = userAddress;
    }

    public OrderAddressDetails getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(OrderAddressDetails shopAddress) {
        this.shopAddress = shopAddress;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<Services> getAddedService() {
        return addedService;
    }

    public void setAddedService(List<Services> addedService) {
        this.addedService = addedService;
    }
}
