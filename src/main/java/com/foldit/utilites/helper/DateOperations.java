package com.foldit.utilites.helper;

import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateOperations.class);

    static ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static boolean validateTheDateFormat(String inputDate) {
        try {
            LocalDate.parse(inputDate, formatter);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public static int batchSizeForSlotsMapping(NegotiationConfigHolder negotiationConfigHolder, String orderDate) {
        int batchSize;
        batchSize = negotiationConfigHolder.getOldTimeSlotInHourForBatchSize();
        LocalDate currentDate = LocalDate.parse(orderDate, formatter);
        LocalDate dateToApplyNewBatchSize = LocalDate.parse(negotiationConfigHolder.getLastDateToShowOldSlotsTimings(), formatter);
        if (currentDate.isAfter(dateToApplyNewBatchSize)) {
            batchSize = negotiationConfigHolder.getNewTimeSlotInHourForBatchSize();
        }
        return batchSize;
    }

}
