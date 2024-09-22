package com.foldit.utilites.negotiationconfigholder.model;

public record ChangeBatchSlotTimingRequest(String lastDateToShowOldSlotsTimings,
                                           Integer newTimeSlotsBatchSizeInHourDifference,
                                           Integer oldTimeSlotsBatchSizeInHourDifference,
                                           String userId) {
}
