package com.foldit.utilites.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class GenerateOtp {

    private static final Logger LOGGER =  LoggerFactory.getLogger(GenerateOtp.class);

    public static String generate4DigitOtpCode() {
        Random random = new Random();
        int randomNumber = random.nextInt(10000);
        return String.format("%04d", randomNumber);
    }

}
