package com.foldit.utilites.store.interfaces;

import com.foldit.utilites.rider.model.RiderDeliveryTask;
import com.foldit.utilites.store.model.SlotsSegregation;

import java.util.List;
import java.util.Map;

public interface IGetTimeSlotsForScheduledPickUp {

    Map<String,Map<SlotsSegregation,List<String>>> getUserTimeSlotsForScheduledPickupDurationWise(String shopStartTime, String shopEndTime);

    Map<String, List<String>> getUserTimeSlotsForScheduledPickUp(String shopStartTime, String shopEndTime);

    Map<String, Map<RiderDeliveryTask, List<String>>> getRiderAdminTimeSlotsForScheduledPickUp(String shopStartTime, String shopEndTime);

}
