package com.foldit.utilites.image.control;

import com.foldit.utilites.image.model.Image;
import com.foldit.utilites.image.model.ImageUploadResponse;
import com.foldit.utilites.image.service.ImageOperationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class ImageOperationsController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(ImageOperationsController.class);

    @Autowired
    private ImageOperationsService imageOperationsService;

    @PostMapping("/photo/upload")
    public ResponseEntity<ImageUploadResponse> uploadPhoto(@RequestParam("image") MultipartFile imageFile) throws IOException {
        String uniqueIdLinkedToFile = imageOperationsService.uploadImage(imageFile);
        return new ResponseEntity<>(new ImageUploadResponse(uniqueIdLinkedToFile), HttpStatus.OK);
    }

    @GetMapping(value = "/photo/fetchFromImageId", produces =  {
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE} )
    public byte[] getPhoto(@RequestParam("imageId")String imageId) throws IOException {
        Image image = imageOperationsService.getPhoto(imageId);
        return  image.getImage();

    }

}
