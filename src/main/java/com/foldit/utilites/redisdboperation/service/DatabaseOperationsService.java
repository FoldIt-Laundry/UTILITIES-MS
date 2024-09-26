package com.foldit.utilites.redisdboperation.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.RedisDBException;
import com.foldit.utilites.rider.model.RiderDeliveryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
            LOGGER.error("validateAuthToken(): Auth validation failed for userId: {} and authToken: {}", userIdVerificationKey, authToken);
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("validateAuthToken(): Exception occured while validating the auth token: {}, Exception: {}", authToken, ex.getMessage());
            throw new AuthTokenValidationException(null);
        }
    }

    public Long addOrderIdInBatchSlot(String orderId, String keyForBatch, RiderDeliveryTask riderDeliveryTask) {
        try {
            return redisTemplate.opsForList().rightPush(keyForBatch + riderDeliveryTask, orderId);
        } catch (Exception ex) {
            LOGGER.error("addOrderIdInBatchSlot(): Exception occurred while adding orderId: {} in given batch slot", orderId, keyForBatch);
        }
        return 0L;
    }


    public String getTheFirstOrderIdInBatchSlot(String keyForBatch, RiderDeliveryTask riderDeliveryTask) {
        try {
            String value = (String) redisTemplate.opsForList().index(keyForBatch+ riderDeliveryTask,0);
            if(value == null) {
                if(redisTemplate.opsForList().size(keyForBatch)==0) return "";
                String errorMessage = String.format("getTheFirstOrderIdInBatchSlot(): Value exists in list for key: %s but db does not return anything", keyForBatch);
                LOGGER.error(errorMessage);
                throw new RedisDBException(errorMessage);
            }
            return value;
        } catch (Exception ex) {
            LOGGER.error("getTheFirstOrderIdInBatchSlot(): Exception occurred while deleting and getting the first orderId for given batch slot key: {}, Exception: {}", keyForBatch, ex.getMessage());
            return "";
        }
    }

    public List<String> getAllOrderIdsInBatchSlot(String keyForBatch, RiderDeliveryTask riderDeliveryTask) {
        try {
            return redisTemplate.opsForList().range(keyForBatch+riderDeliveryTask,0,-1);
        } catch (Exception ex) {
            String errorMessage  = String.format("getAllOrderIdsInBatchSlot(): Exception occurred while getting all the orders present inside the given batch slot key: %s and riderDeliveryTask: %s , Exception: %s", keyForBatch, riderDeliveryTask, ex.getMessage());
            LOGGER.error(errorMessage, ex);
            throw new RedisDBException(errorMessage, ex);
        }
    }

    public void deleteAGivenOrderIdInBatchSlot(String orderId,String keyForBatch, RiderDeliveryTask riderDeliveryTask) {
        try {
            Long count = redisTemplate.opsForList().remove(keyForBatch+riderDeliveryTask,0,orderId);
            if(count==0) {
                String errorMessage  = String.format("deleteAGivenOrderIdInBatchSlot(): Given orderId: %s does not exist in batch slot: %s for task: %s", orderId, keyForBatch, riderDeliveryTask);
                LOGGER.error(errorMessage);
                throw new RedisDBException(errorMessage);
            }
        } catch (Exception ex) {
            String errorMessage  = String.format("deleteAGivenOrderIdInBatchSlot(): Exception occurred while deleting the orderId: %s from the batchSlot: %s and riderDeliveryTask: %s , Exception: %s", orderId, keyForBatch, riderDeliveryTask, ex.getMessage());
            LOGGER.error(errorMessage, ex);
            throw new RedisDBException(errorMessage, ex);
        }
    }

    public void changeIndexOfAGivenValueInList(String keyForBatch, String value, Integer index, RiderDeliveryTask riderDeliveryTask) {
        try {
//            redisTemplate.opsForZSet()
//            redisTemplate.opsForZSet().add(keyForBatch+riderDeliveryTask, value, 1);
//            Long indexAtWhichValueIsPresent = redisTemplate.opsForList().indexOf(keyForBatch+riderDeliveryTask, value);
//            if(indexAtWhichValueIsPresent==null || indexAtWhichValueIsPresent.compareTo(Long.valueOf(index))==0 ) {
//                return;
//            }
//            redisTemplate.opsForList().rightPushAll()
            redisTemplate.opsForList().set(keyForBatch, index, value);
//            if(value == null) {
//                if(redisTemplate.opsForList().size(keyForBatch)==0) return "";
//                String errorMessage = String.format("getTheFirstOrderIdInBatchSlot(): Value exists in list for key: %s but db does not return anything", keyForBatch);
//                LOGGER.error(errorMessage);
//                throw new RedisDBException(errorMessage);
//            }
//            return value;
        } catch (Exception ex) {
            LOGGER.error("getTheFirstOrderIdInBatchSlot(): Exception occurred while deleting and getting the first orderId for given batch slot key: {}, Exception: {}", keyForBatch, ex.getMessage());
            return ;
        }
    }

}
