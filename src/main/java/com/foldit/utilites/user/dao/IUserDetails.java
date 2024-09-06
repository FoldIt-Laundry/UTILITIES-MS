package com.foldit.utilites.user.dao;


import com.foldit.utilites.user.model.UserDetails;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IUserDetails extends MongoRepository<UserDetails, String> {
}
