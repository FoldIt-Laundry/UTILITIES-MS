package com.foldit.utilites.redisdboperation.service;

import com.foldit.utilites.exception.RedisDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseOperationsService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(DatabaseOperationsService.class);

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean validateAuthToken(String userIdVerificationKey,String authToken) {
        try {
            LOGGER.info("validateAuthToken(): Initiating request to validate the userIdVerificationKey: {} and authToken: {}", userIdVerificationKey, authToken);
            String keyForAuthToken = userIdVerificationKey+"AuthToken";
            String storedAuthToken = (String) redisTemplate.opsForValue().get(keyForAuthToken);
            if (storedAuthToken != null && storedAuthToken.equalsIgnoreCase(authToken)) {
                return true;
            }
        } catch (Exception ex) {
            LOGGER.error("validateAuthToken(): Exception occured while validating the auth token: {}, Exception: {}", authToken, ex.getMessage());
        }
        return false;
    }

    public Long addOrderIdInBatchSlot(String orderId, String slotDate, String slotTime) {
        try {
            String keyForBatch = slotDate+slotTime;
            return redisTemplate.opsForList().rightPush(keyForBatch, orderId);
        } catch (Exception ex) {
            LOGGER.error("addOrderIdInBatchSlot(): Exception occurred while adding orderId: {} in given batch slot", orderId, slotDate+slotTime);
        }
        return 0L;
    }

    public String removeAndGetTheFirstOrderIdInBatchSlot(String slotDate, String slotTime) {
        try {
            String keyForBatch = slotDate+slotTime;
            String value = (String) redisTemplate.opsForList().leftPop(keyForBatch);
            if(value == null) {
                if(redisTemplate.opsForList().size(keyForBatch)==0) return "";
                String errorMessage = String.format("removeAndGetTheFirstOrderIdInBatchSlot(): Value exists in list for key: %s but db does not return anything", keyForBatch);
                LOGGER.error(errorMessage);
                throw new RedisDBException(errorMessage);
            }
            return value;
        } catch (Exception ex) {
            LOGGER.error("removeAndGetTheFirstOrderIdInBatchSlot(): Exception occurred while deleting and getting the first orderId for given batch slot key: {}, Exception: {}", slotDate+slotTime, ex.getMessage());
            return "";
        }
    }

}
