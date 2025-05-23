package com.foldit.utilites.store.interfacesimp;

import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.rider.model.RiderDeliveryTask;
import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import com.foldit.utilites.store.model.SlotsSegregation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.foldit.utilites.rider.model.RiderDeliveryTask.DROP;
import static com.foldit.utilites.rider.model.RiderDeliveryTask.PICKUP;
import static com.foldit.utilites.store.model.SlotsSegregation.*;

@Service
public class SlotsGeneratorForScheduledPickup implements IGetTimeSlotsForScheduledPickUp {

    private static final Logger LOGGER =  LoggerFactory.getLogger(SlotsGeneratorForScheduledPickup.class);
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;

    @Override
    public Map<String,Map<SlotsSegregation,List<String>>> getUserTimeSlotsForScheduledPickupDurationWise(String shopStartTime, String shopEndTime) {
        Map<String,Map<SlotsSegregation,List<String>>> schedule = new TreeMap<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        try{
            LocalTime afternoon = LocalTime.parse("12:00 pm", timeFormatter);
            LocalTime evening = LocalTime.parse("04:00 pm", timeFormatter);
            Map<String, List<LocalTime>> dayVsSlotsList = getUserTimeSlotsForScheduledPickUpOnlyStartingTime(shopStartTime, shopEndTime);
            dayVsSlotsList.entrySet().forEach(date -> {
                List<String> eveningTimeSlots = new ArrayList<>();
                List<String> morningTimeSlots = new ArrayList<>();
                List<String> afternoonTimeSlots = new ArrayList<>();
                date.getValue().forEach(timings -> {
                    if(timings.isBefore(afternoon))  {
                        morningTimeSlots.add(timings.format(timeFormatter));
                    } else if(timings.isAfter(evening)) {
                        eveningTimeSlots.add(timings.format(timeFormatter));
                    } else {
                        afternoonTimeSlots.add(timings.format(timeFormatter));
                    }
                });
                Map<SlotsSegregation,List<String>> slotsSegregationListMap = new TreeMap<>();
                if(!CollectionUtils.isEmpty(morningTimeSlots)) {
                    slotsSegregationListMap.put(MORNING, morningTimeSlots);
                }
                if(!CollectionUtils.isEmpty(afternoonTimeSlots)) {
                    slotsSegregationListMap.put(AFTERNOON, afternoonTimeSlots);
                }
                if(!CollectionUtils.isEmpty(eveningTimeSlots)) {
                    slotsSegregationListMap.put(EVENING, eveningTimeSlots);
                }
                schedule.put(date.getKey(), slotsSegregationListMap);
            });
        } catch (Exception ex) {

        }
        return schedule;
    }

    public Map<String, List<LocalTime>> getUserTimeSlotsForScheduledPickUpOnlyStartingTime(String shopStartTime, String shopEndTime) {
        Map<String, List<LocalTime>> schedule = new TreeMap<>();
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

            if(currentTime.getMinute()>44) {
                currentTime = istTime.toLocalTime().withMinute(0).withSecond(0).withNano(0);
                currentTime.plusHours(1);
            }

            LocalDate lastDateToChangeSlotTiming = LocalDate.parse(lastDateToChangeTheSlotTimings, dateFormatter);


            LocalTime openingTime = LocalTime.parse(shopStartTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime closingTime = LocalTime.parse(shopEndTime, DateTimeFormatter.ofPattern("HH:mm"));

            boolean flagNewTimeSlotEligible = false;
            int slotCount = 0;
            LocalDate date = currentDate;
            LocalTime slotStart = openingTime;
            if(closingTime.isBefore(slotStart)) {
                date = date.plusDays(1);
            }

            while (slotCount < maxSlotCountToReturn) {

                if(lastDateToChangeSlotTiming.isBefore(date)) {
                    flagNewTimeSlotEligible = true;
                }

                String dateStr = date.format(dateFormatter);
                List<LocalTime> slots = new ArrayList<>();

                while ( slotStart.isBefore(closingTime) && slotCount < maxSlotCountToReturn) {
                    if ( slotStart.isBefore(closingTime)) {
                        if( date.isAfter(currentDate) || slotStart.isAfter(currentTime)) {
                            slots.add(slotStart);
                            slotCount++;
                        }
                    } else break;
                    int hoursToAdd2 = Math.max(1,(flagNewTimeSlotEligible) ? 2*newSlotDifference : 2*oldSlotDifference);
                    slotStart = slotStart.plusHours(hoursToAdd2);
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

            if(currentTime.getMinute()>44) {
                currentTime = istTime.toLocalTime().withMinute(0).withSecond(0).withNano(0);
                currentTime.plusHours(1);
            }

            LocalDate lastDateToChangeSlotTiming = LocalDate.parse(lastDateToChangeTheSlotTimings, dateFormatter);


            LocalTime openingTime = LocalTime.parse(shopStartTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime closingTime = LocalTime.parse(shopEndTime, DateTimeFormatter.ofPattern("HH:mm"));

            boolean flagNewTimeSlotEligible = false;
            int slotCount = 0;
            LocalDate date = currentDate;
            LocalTime slotStart = openingTime;
            if(closingTime.isBefore(slotStart)) {
                date = date.plusDays(1);
            }

            while (slotCount < maxSlotCountToReturn) {

                if(lastDateToChangeSlotTiming.isBefore(date)) {
                    flagNewTimeSlotEligible = true;
                }

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
                        if( date.isAfter(currentDate) || slotStart.isAfter(currentTime)) {
                            slots.add(slotStartTime + " - " + slotEndTime);
                            slotCount++;
                        }
                    } else break;
                    int hoursToAdd2 = Math.max(1,(flagNewTimeSlotEligible) ? 2*newSlotDifference : 2*oldSlotDifference);
                    slotStart = slotStart.plusHours(hoursToAdd2);
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


    public Map<String, Map<RiderDeliveryTask, List<String>>> getRiderAdminTimeSlotsForScheduledPickUp(String shopStartTime, String shopEndTime) {
        Map<String, Map<RiderDeliveryTask, List<String>>> schedule = new TreeMap<>();
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

            LocalDate lastDateToChangeSlotTiming = LocalDate.parse(lastDateToChangeTheSlotTimings, dateFormatter);


            if (istTime.toLocalTime().getMinute() > 0) {
                currentTime = currentTime.plusHours(1);
            }


            LocalTime openingTime = LocalTime.parse(shopStartTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime closingTime = LocalTime.parse(shopEndTime, DateTimeFormatter.ofPattern("HH:mm"));

            boolean flagNewTimeSlotEligible = false;
            int slotCount = 7;
            LocalDate date = currentDate;
            LocalTime slotStart = openingTime;
            if(closingTime.isBefore(slotStart)) {
                date = date.plusDays(1);
            }

            while (slotCount < maxSlotCountToReturn) {

                if(lastDateToChangeSlotTiming.isBefore(date)) {
                    flagNewTimeSlotEligible = true;
                }

                String dateStr = date.format(dateFormatter);

                Map<RiderDeliveryTask, List<String>> deliveryTaskListMap = new HashMap<>();
                deliveryTaskListMap.put( PICKUP, new ArrayList<>());
                deliveryTaskListMap.put( DROP, new ArrayList<>());

                int flag=0;

                while ( slotStart.isBefore(closingTime) && slotCount < maxSlotCountToReturn) {
                    int hoursToAdd = (flagNewTimeSlotEligible) ? 2*newSlotDifference : 2*oldSlotDifference;

                    if(hoursToAdd==0) {
                        deliveryTaskListMap.get(PICKUP).add(openingTime.format(timeFormatter) + " - " + closingTime.format(timeFormatter));
                        deliveryTaskListMap.get(DROP).add(openingTime.format(timeFormatter) + " - " + closingTime.format(timeFormatter));
                        break;
                    } else if ( slotStart.isBefore(closingTime)) {
                        String slotStartTime = slotStart.format(timeFormatter);
                        String slotEndTime = slotStart.plusHours(hoursToAdd/2).format(timeFormatter).toString();
                        if(slotStart.plusHours(hoursToAdd/2).isAfter(closingTime)) {
                            slotEndTime = closingTime.format(timeFormatter).toString();
                        }
                        if(flag==0) {
                            deliveryTaskListMap.get(PICKUP).add(slotStartTime + " - " + slotEndTime);
                        } else {
                            deliveryTaskListMap.get(DROP).add(slotStartTime + " - " + slotEndTime);
                        }
                        flag = 1-flag;
                    } else break;

                    int hoursToAdd2 = (flagNewTimeSlotEligible) ? newSlotDifference : oldSlotDifference;
                    slotStart = slotStart.plusHours(hoursToAdd2);

                }


                if (deliveryTaskListMap.get(PICKUP).size()>0 || deliveryTaskListMap.get(DROP).size()>0) {
                    schedule.put(dateStr, deliveryTaskListMap);
                }


                slotCount++;
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
