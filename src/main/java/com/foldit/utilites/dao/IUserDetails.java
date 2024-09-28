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

    @Aggregation({
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'fcmToken': 1 }}"
    })
    UserDetails getFcmTokenFromUserId(String userId);

    @Aggregation({
            "{ $project: { 'fcmToken': 1 }}"
    })
    List<UserDetails> getAllUserFcmToken();

    @Aggregation({
            "{ $match: { '_id': {'$in':  ?0} }},",
            "{ $project: { 'fcmToken': 1 }}"
    })
    List<UserDetails> getFcmTokenFromUserIdList(List<String> userIdList);

}
