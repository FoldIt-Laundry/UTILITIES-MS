package com.foldit.utilites.redisdboperation.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.redisdboperation.interfaces.TokenValidation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenValidationService implements TokenValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenValidationService.class);
    @Autowired
    private DatabaseOperationsService databaseOperationsService;

    @Override
    @Transactional(readOnly = true)
    public boolean authTokenValidationFromUserOrMobile(String authToken, String userId, String mobileNumber) {
        try {
            LOGGER.info("authTokenValidationFromUserOrMobile(): Request received to validate the auth token details: authToken: {}, userId: {}, mobileNumber: {}", authToken, userId, mobileNumber);
            if (StringUtils.isNotBlank((userId)) && databaseOperationsService.validateAuthToken(userId, authToken)) {
                return true;
            }
            if (StringUtils.isNotBlank((mobileNumber)) && databaseOperationsService.validateAuthToken(mobileNumber, authToken)) {
                return true;
            }
            LOGGER.error("Auth token: {}, Validation failed", authToken);
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("authTokenValidation(): Exception occurred while validating the auth token: {} from either userId: {} or mobileNumber: {}", authToken, userId, mobileNumber);
            throw new AuthTokenValidationException(null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean authTokenValidationFromUserId(String authToken, String userId) {
        try {
            if(!databaseOperationsService.validateAuthToken(userId, authToken)) {
                throw new AuthTokenValidationException(null);
            }
            return true;
        } catch (Exception ex) {
            LOGGER.error("authTokenValidationFromUserId(): Exception occurred while validating the auth token: {} from userId: {}", authToken, userId);
            throw new AuthTokenValidationException(null);
        }
    }
}
