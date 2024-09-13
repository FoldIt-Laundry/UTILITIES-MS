package com.foldit.utilites.tokenverification.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.tokenverification.interfaces.TokenValidation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenValidationService implements TokenValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenValidationService.class);
    @Autowired
    private RedisTokenVerificationService redisTokenVerificationService;

    @Override
    public boolean authTokenValidationFromUserOrMobile(String authToken, String userId, String mobileNumber) {
        try {
            LOGGER.info("authTokenValidationFromUserOrMobile(): Request received to validate the auth token details: authToken: {}, userId: {}, mobileNumber: {}", authToken, userId, mobileNumber);
            if (StringUtils.isNotBlank((userId)) && redisTokenVerificationService.validateAuthToken(userId, authToken)) {
                return true;
            }
            if (StringUtils.isNotBlank((mobileNumber)) && redisTokenVerificationService.validateAuthToken(mobileNumber, authToken)) {
                return true;
            }
            LOGGER.error("Auth token: {}, Validation failed", authToken);
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("authTokenValidation(): Exception occurred while validating the auth token: {}", authToken);
            throw new AuthTokenValidationException(null);
        }
    }
}
