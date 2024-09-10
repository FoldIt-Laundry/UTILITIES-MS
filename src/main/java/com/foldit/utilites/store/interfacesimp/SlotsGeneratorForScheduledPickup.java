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
        Map<String, List<String>> schedule = new TreeMap<>();
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now().withMinute(0).withSecond(0).withNano(0);


            if (LocalTime.now().getMinute() > 0) {
                currentTime = currentTime.plusHours(1);
            }


            LocalTime openingTime = LocalTime.parse(shopStartTime, DateTimeFormatter.ofPattern("HH:mm")).withMinute(0).withSecond(0).withNano(0);
            LocalTime closingTime = LocalTime.parse(shopEndTime, DateTimeFormatter.ofPattern("HH:mm")).withMinute(0).withSecond(0).withNano(0);


            int slotCount = 0;
            LocalDate date = currentDate;
            LocalTime slotStart = openingTime;


            while (slotCount < 20) {

                String dateStr = date.format(dateFormatter);
                List<String> slots = schedule.getOrDefault(dateStr, new ArrayList<>());


                while (!slotStart.isAfter(closingTime.minusHours(1)) && slotCount < 20) {

                    if (date.isAfter(currentDate) || (date.equals(currentDate) && slotStart.isAfter(currentTime))) {
                        slots.add(slotStart.format(timeFormatter) + " - " + slotStart.plusHours(1).format(timeFormatter));
                        slotCount++;
                    }
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
