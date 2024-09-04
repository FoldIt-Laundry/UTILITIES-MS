package com.foldit.utilites.image.model;

public class ImageUploadResponse {
    private String uniqueId;

    public ImageUploadResponse(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
