package com.foldit.utilites.exception;

import com.foldit.utilites.homepage.control.HomePageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class FoldItExceptionHandler {

    private static final Logger LOGGER =  LoggerFactory.getLogger(FoldItExceptionHandler.class);

    @ExceptionHandler(value = MongoDBInsertionException.class)
    public Map<String,String> throwDataBaseInsertionException(MongoDBInsertionException mongoDBInsertionException) {
        Map<String,String> map_ = new HashMap<>();
        map_.put("errorMessage", mongoDBInsertionException.getMessage());
        return map_;
    }

    @ExceptionHandler(value = RedisDBException.class)
    public Map<String,String> throwRedisReadException(RedisDBException redisReadException) {
        Map<String,String> map_ = new HashMap<>();
        map_.put("errorMessage", redisReadException.getMessage());
        return map_;
    }

    @ExceptionHandler(value = MongoDBReadException.class)
    public Map<String,String> throwMongoDBReadException(MongoDBReadException mongoDBReadException) {
        Map<String,String> map_ = new HashMap<>();
        map_.put("errorMessage", "Failed to read data from MongoDB");
        return map_;
    }

    @ExceptionHandler(value = AuthTokenValidationException.class)
    public Map<String,String> throwAuthTokenValidationException(AuthTokenValidationException authTokenValidationException) {
        Map<String,String> map_ = new HashMap<>();
        map_.put("errorMessage", "Failed to read data from MongoDB");
        return map_;
    }

    @ExceptionHandler(value = GoogleApiException.class)
    public Map<String,String> throwGoogleApiException(GoogleApiException googleApiException) {
        Map<String,String> map_ = new HashMap<>();
        map_.put("errorMessage", String.format("Failed to get the distance data from the google api, Exception: %s", googleApiException.getMessage()));
        return map_;
    }

    @ExceptionHandler(value = RecordsValidationException.class)
    public Map<String,String> throwRecordsValidationException(RecordsValidationException recordsValidationException) {
        Map<String,String> map_ = new HashMap<>();
        map_.put("errorMessage", String.format("Wrong input data provide, Please provide the correct data, Exception: %s", recordsValidationException.getMessage()));
        return map_;
    }

}
