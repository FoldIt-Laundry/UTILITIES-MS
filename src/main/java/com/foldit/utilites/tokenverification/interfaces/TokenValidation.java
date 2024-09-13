package com.foldit.utilites.tokenverification.interfaces;

public interface TokenValidation {

    boolean authTokenValidationFromUserOrMobile(String authToken, String userId, String mobileNumber);
}
