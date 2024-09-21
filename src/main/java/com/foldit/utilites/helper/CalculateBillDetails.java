package com.foldit.utilites.helper;

import com.foldit.utilites.exception.RecordsValidationException;
import com.foldit.utilites.order.model.CostStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CalculateBillDetails {

    private static final Logger LOGGER =  LoggerFactory.getLogger(CalculateBillDetails.class);

    public static CostStructure getFinalBillDetailsFromQuantity(Map<String,Double> serviceIdVsPrice, Map<String,Double> serviceIdVsQuantity) {
        CostStructure costStructure = new CostStructure();
        final double[] finalPrice = {0};

        serviceIdVsQuantity.entrySet().forEach( inputServiceIdAndQuantity -> {
            if(!serviceIdVsQuantity.containsKey(inputServiceIdAndQuantity.getKey())) {
                String message = "";
                LOGGER.error(message);
                throw new RecordsValidationException(message);
            }
            finalPrice[0]+=serviceIdVsPrice.get(inputServiceIdAndQuantity.getKey())*inputServiceIdAndQuantity.getValue();
        });

        costStructure.setFinalPrice(finalPrice[0]);
        costStructure.setItemTotal(finalPrice[0]);
        return costStructure;
    }

}
