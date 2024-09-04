package com.foldit.utilites.order.model;

public class CostStructure {
    private double itemTotal;
    private Double platformFee;
    private int platformFeeDiscountPercentage;
    private Double deliveryFee;
    private Double taxes;

    public double getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(double itemTotal) {
        this.itemTotal = itemTotal;
    }

    public Double getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(Double platformFee) {
        this.platformFee = platformFee;
    }

    public int getPlatformFeeDiscountPercentage() {
        return platformFeeDiscountPercentage;
    }

    public void setPlatformFeeDiscountPercentage(int platformFeeDiscountPercentage) {
        this.platformFeeDiscountPercentage = platformFeeDiscountPercentage;
    }

    public Double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(Double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public Double getTaxes() {
        return taxes;
    }

    public void setTaxes(Double taxes) {
        this.taxes = taxes;
    }
}
