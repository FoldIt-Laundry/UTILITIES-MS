package com.foldit.utilites.negotiationconfigholder;

import com.foldit.utilites.dao.IConfigDetails;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.foldit.utilites.negotiationconfigholder.constant.NegotiationConstant.*;

@Service
public class NegotiationConfigHolder {

    private static final Logger LOGGER =  LoggerFactory.getLogger(NegotiationConfigHolder.class);

    private String googleApiKeyForDistanceMatrix;
    private String defaultShopId;
    private Integer oldTimeSlotInHourForBatchSize;
    private Integer newTimeSlotInHourForBatchSize;
    private Integer timeSlotQuantityToShow;
    private String lastDateToShowOldSlotsTimings;

    @Autowired
    private IConfigDetails iConfigDetails;


    @PostConstruct
    public void populateConfigurations() {
        googleApiKeyForDistanceMatrix = populateNegotiationConfig(GOOGLE_API_KEY_FOR_DISTANCE_MATRIX);
        defaultShopId = populateNegotiationConfig(DEFAULT_STORE_ID);
        oldTimeSlotInHourForBatchSize = Integer.parseInt(populateNegotiationConfig(OLD_TIME_SLOTS_BATCH_SIZE_IN_HOUR_DIFFERENCE));
        newTimeSlotInHourForBatchSize = Integer.parseInt(populateNegotiationConfig(NEW_TIME_SLOTS_BATCH_SIZE_IN_HOUR_DIFFERENCE));
        timeSlotQuantityToShow = Integer.parseInt(populateNegotiationConfig(TIME_SLOTS_QUANTITY_TO_SHOW));
        lastDateToShowOldSlotsTimings= populateNegotiationConfig(LAST_DATE_TO_SHOW_OLD_SLOTS_TIMINGS);
    }

    public String populateNegotiationConfig(String key) {
        return iConfigDetails.getConfigValue(key).getConfigValue();
    }

    public Integer getOldTimeSlotInHourForBatchSize() {
        return oldTimeSlotInHourForBatchSize;
    }

    public Integer getTimeSlotQuantityToShow() {
        return timeSlotQuantityToShow;
    }

    public Integer getNewTimeSlotInHourForBatchSize() {
        return newTimeSlotInHourForBatchSize;
    }

    public String getLastDateToShowOldSlotsTimings() {
        return lastDateToShowOldSlotsTimings;
    }

    public String getGoogleApiKeyForDistanceMatrix() {
        return googleApiKeyForDistanceMatrix;
    }

    public String getDefaultShopId() {
        return defaultShopId;
    }

    public void refreshTimeSlotQuantityToShow() {
        timeSlotQuantityToShow = Integer.parseInt(populateNegotiationConfig("TIME_SLOTS_QUANTITY_TO_SHOW"));
    }

    public void refreshBatchSlotTimings() {
        oldTimeSlotInHourForBatchSize = Integer.parseInt(populateNegotiationConfig(OLD_TIME_SLOTS_BATCH_SIZE_IN_HOUR_DIFFERENCE));
        newTimeSlotInHourForBatchSize = Integer.parseInt(populateNegotiationConfig(NEW_TIME_SLOTS_BATCH_SIZE_IN_HOUR_DIFFERENCE));
        lastDateToShowOldSlotsTimings= populateNegotiationConfig(LAST_DATE_TO_SHOW_OLD_SLOTS_TIMINGS);
    }

}
