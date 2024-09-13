package com.foldit.utilites.helper;


import com.foldit.utilites.exception.GoogleApiException;
import com.foldit.utilites.store.model.DeliveryFeeCalculatorRequest;
import com.foldit.utilites.user.model.DeliveryAndFeeDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

public class GoogleMatrixForDeliveryFee {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleMatrixForDeliveryFee.class);
    static RestTemplate restTemplate = new RestTemplate();

    public static double calculateDeliveryFee(DeliveryFeeCalculatorRequest deliveryFeeCalculatorRequest, Double priceForOutOfRangeKm, Double freeDeliveryDistanceAllowed) {
        try {
            Double distance = calculateDistance(deliveryFeeCalculatorRequest);
            if (distance >= freeDeliveryDistanceAllowed) return distance * priceForOutOfRangeKm;
            return 0;
        } catch (Exception ex) {
            LOGGER.error("calculateDeliveryFee(): Exception occurred while getting the distance data from the google api matrix, Exception : {}", ex.getMessage());
            throw new GoogleApiException(ex.getMessage(), ex);
        }
    }

    public static DeliveryAndFeeDetails getDeliveryFeeAndDistanceDetails(DeliveryFeeCalculatorRequest deliveryFeeCalculatorRequest, Double priceForOutOfRangeKm, Double freeDeliveryDistanceAllowed) {
        try {
            Double distance = calculateDistance(deliveryFeeCalculatorRequest);
            Double deliveryPrice = (double) 0;
            if (distance >= freeDeliveryDistanceAllowed) deliveryPrice = distance * priceForOutOfRangeKm;
            return new DeliveryAndFeeDetails(distance, deliveryPrice);
        } catch (Exception ex) {
            LOGGER.error("calculateDeliveryFee(): Exception occurred while getting the distance data from the google api matrix, Exception : {}", ex.getMessage());
            throw new GoogleApiException(ex.getMessage(), ex);
        }
    }


    private static double calculateDistance(DeliveryFeeCalculatorRequest deliveryFeeCalculatorRequest) {
        String source = deliveryFeeCalculatorRequest.getSourceLatitude() + "," + deliveryFeeCalculatorRequest.getSourceLongitude();
        String destination = deliveryFeeCalculatorRequest.getDestinationLatitude() + "," + deliveryFeeCalculatorRequest.getDestinationLongitude();
        Double distance = null;
        String url = String.format(
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=%s&key=%s",
                source, destination, ""
        );
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        LOGGER.info("calculateDistance(): Response received from google api matrix is: {}", toJson(response.getBody()));
        String regex = "distance=\\{text=([0-9.]+\\s*km),";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response.getBody().toString());

        if (matcher.find()) {
            distance = Double.valueOf(matcher.group(1).split(" ")[0]); // Extracts the distance text (25.3 km)
            System.out.println("Distance: " + distance);
        } else {
            System.out.println("Distance not found in the input string.");
        }
        return distance;
    }

}
