package com.foldit.utilites.store.interfacesimp;

import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SlotsGeneratorForScheduledPickup implements IGetTimeSlotsForScheduledPickUp {

    private static final Logger LOGGER =  LoggerFactory.getLogger(SlotsGeneratorForScheduledPickup.class);
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<String, List<String>> getTimeSlotsForScheduledPickUp(String shopStartTime, String shopEndTime) {
        Map<String, List<String>> schedule = new TreeMap<>();
        try {
            ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate currentDate = istTime.toLocalDate();
            LocalTime currentTime = istTime.toLocalTime().withMinute(0).withSecond(0).withNano(0);


            if (istTime.toLocalTime().getMinute() > 0) {
                currentTime = currentTime.plusHours(1);
            }


            LocalTime openingTime = LocalTime.parse(shopStartTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime closingTime = LocalTime.parse(shopEndTime, DateTimeFormatter.ofPattern("HH:mm"));


            int slotCount = 0;
            LocalDate date = currentDate;
            LocalTime slotStart = currentTime;


            while (slotCount < 20) {

                String dateStr = date.format(dateFormatter);
                List<String> slots = new ArrayList<>();


                while ( slotStart.isBefore(closingTime) && slotCount < 20) {

                    if ( slotStart.isBefore(closingTime)) {
                        slots.add(slotStart.format(timeFormatter) + " - " + slotStart.plusHours(1).format(timeFormatter));
                        slotCount++;
                    } else break;
                    slotStart = slotStart.plusHours(1);
                }


                if (!slots.isEmpty()) {
                    schedule.put(dateStr, slots);
                }


                date = date.plusDays(1);
                slotStart = openingTime;
            }

            return schedule;
        } catch (Exception ex) {
            LOGGER.error("getTimeSlotsForScheduledPickUp(): Exception occurred while getting the time slots for the scheduled pickup, Exception: %s", ex.getMessage());
        }
        return new HashMap<>();
    }
}
