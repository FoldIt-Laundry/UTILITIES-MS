package com.foldit.utilites.store.interfaces;

import java.util.List;
import java.util.Map;

public interface IGetTimeSlotsForScheduledPickUp {

    Map<String, List<String>> getTimeSlotsForScheduledPickUp(String shopStartTime, String shopEndTime);

}
