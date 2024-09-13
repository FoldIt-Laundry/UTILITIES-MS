package com.foldit.utilites.image.service;

import com.foldit.utilites.dao.IImage;
import com.foldit.utilites.image.model.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImageOperationsService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(ImageOperationsService.class);

    @Autowired
    private IImage iImage;

    public String uploadImage(MultipartFile imageFile) throws IOException {
        Image image = new Image();
        image.setImage(imageFile.getBytes());
        Image uploadedImageResponse = iImage.insert(image);
        return uploadedImageResponse.getId();
    }

    public Image getPhoto(String imageId) {
        return  iImage.findById(imageId).get();
    }

}
