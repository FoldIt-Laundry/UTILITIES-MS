package com.foldit.utilites.dao;

import com.foldit.utilites.homepage.model.Services;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IServiceOffered extends MongoRepository<Services,String> {
}
