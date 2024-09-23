package com.foldit.utilites.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ValidateTheDateFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateTheDateFormat.class);

    static ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static boolean validateTheDateFormat(String inputDate) {
        try {
            LocalDate currentDate = istTime.toLocalDate();
            LocalDate input = LocalDate.parse(inputDate,formatter);
            if(input.isBefore(currentDate)) return  false;
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

}
