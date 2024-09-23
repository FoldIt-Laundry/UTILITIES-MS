package com.foldit.utilites.negotiationconfigholder.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.negotiationconfigholder.model.ChangeBatchSlotTimingRequest;
import com.foldit.utilites.negotiationconfigholder.model.Configuration;
import com.foldit.utilites.redisdboperation.service.DatabaseOperationsService;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.helper.DateOperations.validateTheDateFormat;
import static com.foldit.utilites.negotiationconfigholder.constant.NegotiationConstant.*;

@Service
public class NegotiationConfigService {
    private static final Logger LOGGER =  LoggerFactory.getLogger(NegotiationConfigService.class);

    @Autowired
    private DatabaseOperationsService databaseOperationsService;
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional
    public void  changeBatchSlotTimings(String authToken, String userRole, ChangeBatchSlotTimingRequest request) {
        try {
            databaseOperationsService.validateAuthToken(request.userId(), authToken);
            if(!userRole.equalsIgnoreCase("SUPER_ADMIN") || !validationChangeBatchSlotTimingsRequest(request)) {
                LOGGER.error("changeSlotsQuantityToShow(): Given userId is not super admin or validation failed for request: {} , supplied userId: {} and userRole: {}", request.userId(), toJson(request), userRole);
                throw new AuthTokenValidationException(null);
            }

            updateConfigurationInDbFromInputInteger(NEW_TIME_SLOTS_BATCH_SIZE_IN_HOUR_DIFFERENCE, request.newTimeSlotsBatchSizeInHourDifference(),request.userId());

            updateConfigurationInDbFromInputInteger(OLD_TIME_SLOTS_BATCH_SIZE_IN_HOUR_DIFFERENCE, request.oldTimeSlotsBatchSizeInHourDifference(),request.userId());

            updateConfigurationInDbFromInputString(LAST_DATE_TO_SHOW_OLD_SLOTS_TIMINGS, request.lastDateToShowOldSlotsTimings(),request.userId());

            negotiationConfigHolder.refreshBatchSlotTimings();
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("changeSlotsQuantityToShow(): Exception occured while getting all the order details from userId: {} details from monogoDb, Exception: %s", request.userId(), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional
    public void  changeSlotsQuantityToShow(String authToken,String userRole,Integer slotsQuantity,String userId) {
        try {
            databaseOperationsService.validateAuthToken(userId, authToken);
            if(!userRole.equalsIgnoreCase("SUPER_ADMIN") || slotsQuantity<5) {
                LOGGER.error("changeSlotsQuantityToShow(): Given userId is not super admin, supplied userId: {} and userRole: {}", userId, userRole);
                throw new AuthTokenValidationException(null);
            }
            updateConfigurationInDbFromInputInteger(TIME_SLOTS_QUANTITY_TO_SHOW, slotsQuantity,userId);
            negotiationConfigHolder.refreshTimeSlotQuantityToShow();
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("changeSlotsQuantityToShow(): Exception occured while getting all the order details from userId: {} details from monogoDb, Exception: %s", userId, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    public void updateConfigurationInDbFromInputInteger(String key,Integer value, String userId) {
        Query query = new Query(Criteria.where("configKey").is(key));
        Update update = new Update().set("configValue", value);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Configuration.class);
        if(updateResult.getModifiedCount()==0 && updateResult.getMatchedCount()==0) {
            String errorMessage = String.format("changeSlotsQuantityToShow(): No matching records exits in db with given query: %s and update: %s and userId: %s",query, update, userId);
            LOGGER.error(errorMessage);
            throw new AuthTokenValidationException(errorMessage);
        }
    }

    @Transactional
    public void updateConfigurationInDbFromInputString(String key,String value, String userId) {
        Query query = new Query(Criteria.where("configKey").is(key));
        Update update = new Update().set("configValue", value);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Configuration.class);
        if(updateResult.getModifiedCount()==0 && updateResult.getMatchedCount()==0) {
            String errorMessage = String.format("changeSlotsQuantityToShow(): No matching records exits in db with given query: %s and update: %s and userId: %s",query, update, userId);
            LOGGER.error(errorMessage);
            throw new AuthTokenValidationException(errorMessage);
        }
    }

    private boolean validationChangeBatchSlotTimingsRequest(ChangeBatchSlotTimingRequest request) {
        if(StringUtils.isBlank(request.lastDateToShowOldSlotsTimings())) return false;
        if(request.oldTimeSlotsBatchSizeInHourDifference()<0 || request.newTimeSlotsBatchSizeInHourDifference()<0) return false;
        if(request.oldTimeSlotsBatchSizeInHourDifference()>3 || request.newTimeSlotsBatchSizeInHourDifference()>3) return false;
        if(!validateTheDateFormat(request.lastDateToShowOldSlotsTimings())) return false;
        return true;
    }

}
