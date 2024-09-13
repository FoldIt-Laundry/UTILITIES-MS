package com.foldit.utilites.dao;

import com.foldit.utilites.image.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IImage extends MongoRepository<Image, String> {
}
