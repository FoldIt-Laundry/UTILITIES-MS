package com.foldit.utilites.tokenverification.service;

import com.foldit.utilites.homepage.control.HomePageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenVerificationService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(TokenVerificationService.class);

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean validateAuthToken(String userId,String authToken) {
        try {
            String keyForAuthToken = userId+"AuthToken";
            String storedAuthToken = (String) redisTemplate.opsForValue().get(keyForAuthToken);
            if (storedAuthToken != null && storedAuthToken.equalsIgnoreCase(authToken)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            LOGGER.error("validateAuthToken(): Exception occured while validating the auth token: {}, Exception: {}", authToken, ex.getMessage());
        }
        return false;
    }

    public boolean validateAuthTokenFromMobileNumber(String mobileNumber,String authToken) {
        try {
            String keyForAuthToken = mobileNumber+"AuthToken";
            String storedAuthToken = (String) redisTemplate.opsForValue().get(keyForAuthToken);
            if (storedAuthToken != null && storedAuthToken.equalsIgnoreCase(authToken)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            LOGGER.error("validateAuthToken(): Exception occured while validating the auth token: {}, Exception: {}", authToken, ex.getMessage());
        }
        return false;
    }

}
