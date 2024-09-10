package com.foldit.utilites.store.interfaces;

public interface TokenValidation {

    boolean authTokenValidation(String authToken,String userId, String mobileNumber);
}
