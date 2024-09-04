package com.foldit.utilites.exception;

public class MongoDBReadException extends RuntimeException{
    public MongoDBReadException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }

    public MongoDBReadException(String errorMessage) {
        super(errorMessage);
    }
}
