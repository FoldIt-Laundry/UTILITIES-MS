package com.foldit.utilites.store.interfacesimp;

import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SlotsGeneratorForScheduledPickup implements IGetTimeSlotsForScheduledPickUp {

    private static final Logger LOGGER =  LoggerFactory.getLogger(SlotsGeneratorForScheduledPickup.class);
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<String, List<String>> getTimeSlotsForScheduledPickUp(String shopStartTime, String shopEndTime) {
        Map<String, List<String>> slotsMap = new TreeMap<>();
        try {

            ZoneId zoneId = ZoneId.of("Asia/Kolkata");
            LocalTime currentTime = LocalTime.now(zoneId);
            LocalDate currentDate = LocalDate.now(zoneId);

            DateTimeFormatter inputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime openTime = LocalTime.parse(shopStartTime, inputTimeFormatter);
            LocalTime closeTime = LocalTime.parse(shopEndTime, inputTimeFormatter);

            boolean crossesMidnight = closeTime.isBefore(openTime);

            LocalDateTime startDateTime;
            LocalDateTime endDateTime;

            if (crossesMidnight) {
                if (currentTime.isBefore(closeTime)) {
                    startDateTime = LocalDateTime.of(currentDate.minusDays(1), openTime);
                    endDateTime = LocalDateTime.of(currentDate, closeTime);
                } else {
                    startDateTime = LocalDateTime.of(currentDate, openTime);
                    endDateTime = LocalDateTime.of(currentDate.plusDays(1), closeTime);
                }
            } else {
                startDateTime = LocalDateTime.of(currentDate, openTime);
                endDateTime = LocalDateTime.of(currentDate, closeTime);
                if (currentTime.isAfter(closeTime)) {
                    startDateTime = startDateTime.plusDays(1);
                    endDateTime = endDateTime.plusDays(1);
                }
            }

            if (startDateTime.toLocalTime().isBefore(currentTime)) {
                startDateTime = LocalDateTime.of(currentDate, currentTime.plusMinutes(60 - currentTime.getMinute()));
            }

            for (int i = 0; i < 20; i++) {
                if (startDateTime.toLocalTime().isAfter(closeTime) && !crossesMidnight) {
                    startDateTime = startDateTime.plusDays(1).withHour(openTime.getHour()).withMinute(0);
                    endDateTime = endDateTime.plusDays(1).withHour(closeTime.getHour());
                }

                String dateKey = startDateTime.toLocalDate().format(dateFormatter);

                String slot = startDateTime.toLocalTime().format(timeFormatter) + " - "
                        + startDateTime.plusHours(1).toLocalTime().format(timeFormatter);

                slotsMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(slot);

                startDateTime = startDateTime.plusHours(1);
            }

            return slotsMap;
        } catch (Exception ex) {
            LOGGER.error("getTimeSlotsForScheduledPickUp(): Exception occurred while getting the time slots for the scheduled pickup, Exception: %s", ex.getMessage());
        }
        return new HashMap<>();
    }
}
