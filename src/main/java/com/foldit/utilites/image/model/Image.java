package com.foldit.utilites.image.model;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Images")
public class Image {

    private String id;
    private byte[] image;

    // private Timestamp timestamp; can be added later in all the modules


    public Image() {
    }

    public Image(String id, byte[] image) {
        this.id = id;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

}
