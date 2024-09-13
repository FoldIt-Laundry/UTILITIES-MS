package com.foldit.utilites.dao;


import com.foldit.utilites.user.model.UserDetails;
import com.foldit.utilites.user.model.UserLocation;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IUserDetails extends MongoRepository<UserDetails, String> {

    @Aggregation({
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'locations': 1 , '_id': 0 }}"
    })
    UserDetails getAllUserLocationFromUserId(String userId);

}
