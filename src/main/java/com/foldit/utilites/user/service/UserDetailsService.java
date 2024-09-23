package com.foldit.utilites.user.service;

import com.foldit.utilites.redisdboperation.service.TokenValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(UserDetailsService.class);

    @Autowired
    private TokenValidationService tokenValidationService;
    @Autowired
    private MongoTemplate mongoTemplate;

    public void saveFcmToken(String authToken, String userId, String fcmToken) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, userId);
            Query query = new Query(Criteria.where("_id").is(userId));
            Update update = new Update().set("fcmToken", fcmToken);
            mongoTemplate.updateFirst(query, update, "UserDetails");
        } catch (Exception ex) {

        }
    }

}
