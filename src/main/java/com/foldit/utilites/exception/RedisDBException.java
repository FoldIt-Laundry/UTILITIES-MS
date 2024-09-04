package com.foldit.utilites.exception;

public class RedisDBException extends RuntimeException{
    public RedisDBException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }

    public RedisDBException(String errorMessage) {
        super(errorMessage);
    }
}
