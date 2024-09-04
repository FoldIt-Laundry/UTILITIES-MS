package com.foldit.utilites.user.dao;

import com.foldit.utilites.user.model.UserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IUserDetails extends MongoRepository<UserDetails, String> {
}
