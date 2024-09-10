package com.foldit.utilites.store.interfacesimp;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.store.interfaces.TokenValidation;
import com.foldit.utilites.tokenverification.service.TokenVerificationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisAuthTokenValidation implements TokenValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisAuthTokenValidation.class);
    @Autowired
    private TokenVerificationService tokenVerificationService;

    @Override
    public boolean authTokenValidation(String authToken, String userId, String mobileNumber) {
        try {
            if (StringUtils.isNotBlank((userId)) && tokenVerificationService.validateAuthToken(userId, authToken)) {
                return true;
            }
            if (StringUtils.isNotBlank((mobileNumber)) && tokenVerificationService.validateAuthToken(mobileNumber, authToken)) {
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
