package com.foldit.utilites.store.interfacesimp;

import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SlotsGeneratorForScheduledPickup implements IGetTimeSlotsForScheduledPickUp {

    private static final Logger LOGGER =  LoggerFactory.getLogger(SlotsGeneratorForScheduledPickup.class);
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;

    @Override
    public Map<String, List<String>> getUserTimeSlotsForScheduledPickUp(String shopStartTime, String shopEndTime) {
        Map<String, List<String>> schedule = new TreeMap<>();
        try {
            int maxSlotCountToReturn = negotiationConfigHolder.getTimeSlotQuantityToShow();
            int oldSlotDifference = negotiationConfigHolder.getOldTimeSlotInHourForBatchSize();
            int newSlotDifference = negotiationConfigHolder.getNewTimeSlotInHourForBatchSize();
            String lastDateToChangeTheSlotTimings = negotiationConfigHolder.getLastDateToShowOldSlotsTimings();

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

            boolean flagNewTimeSlotEligible = false;
            int slotCount = 0;
            LocalDate date = currentDate;
            LocalTime slotStart = currentTime;
            if(slotStart.isBefore(openingTime)) {
                slotStart = openingTime;
            }

            while (slotCount < maxSlotCountToReturn) {

                String dateStr = date.format(dateFormatter);
                List<String> slots = new ArrayList<>();


                while ( slotStart.isBefore(closingTime) && slotCount < maxSlotCountToReturn) {
                    int hoursToAdd = Math.max(2,(flagNewTimeSlotEligible) ? 2*newSlotDifference : 2*oldSlotDifference);
                    if ( slotStart.isBefore(closingTime)) {
                        String slotStartTime = slotStart.format(timeFormatter);
                        String slotEndTime = slotStart.plusHours(hoursToAdd/2).format(timeFormatter).toString();
                        if(slotStart.plusHours(hoursToAdd/2).isAfter(closingTime)) {
                            slotEndTime = closingTime.format(timeFormatter).toString();
                        }
                        slots.add(slotStartTime + " - " + slotEndTime);
                        slotCount++;
                    } else break;

                    int hoursToAdd2 = Math.max(1,(flagNewTimeSlotEligible) ? 2*newSlotDifference : 2*oldSlotDifference);
                    slotStart = slotStart.plusHours(hoursToAdd2);
                }


                if (!slots.isEmpty()) {
                    schedule.put(dateStr, slots);
                }

                if(date.toString().equalsIgnoreCase(lastDateToChangeTheSlotTimings)) {
                    flagNewTimeSlotEligible = true;
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
