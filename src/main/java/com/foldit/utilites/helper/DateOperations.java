package com.foldit.utilites.helper;

import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.shopadmin.model.MarkOrderOutForDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

public class DateOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateOperations.class);

    static ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

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

    public static boolean isAdminAllowedToMarkTheOrderOutForPickupForGivenSlot(MarkOrderOutForDelivery orderRequest) {
        try {
            LocalDate timeSlotDate = LocalDate.parse(orderRequest.timeSlotDate(), formatter);
            LocalDate currentDate = istTime.toLocalDate();

            LocalTime localTime = istTime.toLocalTime();

            LocalTime inputSlotTime = LocalTime.parse(orderRequest.timeSlotTime(), DateTimeFormatter.ofPattern("hh:mm a"));
            LocalTime timeSlotTime = LocalTime.parse(inputSlotTime.format(DateTimeFormatter.ofPattern("HH:mm")), DateTimeFormatter.ofPattern("HH:mm"));

            LOGGER.info("isAdminAllowedToMarkTheOrderOutForPickupForGivenSlot(): Current timeSlotDate: {} and currentDate: {}; localTime: {} and timeSlotTime", timeSlotDate, currentDate, localTime, timeSlotTime);

            if (timeSlotDate.isEqual(currentDate) && Duration.between(localTime, timeSlotTime).toMinutes() <= 35)
                return true;
        } catch (Exception ex) {
            LOGGER.error("isAdminAllowedToMarkTheOrderOutForPickupForGivenSlot(): Exception occurred while getting the details either admin allowed to mark the order or not for request: {}", toJson(orderRequest));
        }
        return false;
    }

}
