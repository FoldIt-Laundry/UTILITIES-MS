package com.foldit.utilites.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonPrinter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPrinter.class);

    public static String toJson(Object obj) {
        String json = StringUtils.EMPTY;
        try {
            Gson gson =  new GsonBuilder().setPrettyPrinting().create();
            json = gson.toJson(obj);
        } catch (Exception ex) {
            LOGGER.error("Please provide correct data format");
        }
        return  json;
    }

    public static String toJsonText(Object obj) {
        String json = StringUtils.EMPTY;
        try {
            Gson gson =  new GsonBuilder().create();
            json = gson.toJson(obj);
        } catch (Exception ex) {
            LOGGER.error("Please provide correct data format");
        }
        return  json;
    }

}
