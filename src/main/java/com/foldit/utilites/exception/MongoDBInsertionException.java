package com.foldit.utilites.exception;

public class MongoDBInsertionException extends RuntimeException{
    public MongoDBInsertionException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }

    public MongoDBInsertionException(String errorMessage) {
        super(errorMessage);
    }

}
