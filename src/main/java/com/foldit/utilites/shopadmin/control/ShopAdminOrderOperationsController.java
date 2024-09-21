package com.foldit.utilites.shopadmin.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.shopadmin.model.AddOrderQuantityRequest;
import com.foldit.utilites.shopadmin.model.AddOrderQuantityResponse;
import com.foldit.utilites.shopadmin.service.ShopAdminOrderOperationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@RestController
public class ShopAdminOrderOperationsController {
    private static final Logger LOGGER =  LoggerFactory.getLogger(ShopAdminOrderOperationsController.class);

    @Autowired
    private ShopAdminOrderOperationsService shopAdminOrderOperationsService;

    @PatchMapping("/admin/addOrderQuantityDetails")
    public ResponseEntity<AddOrderQuantityResponse> addOrderQuantityDetails(@RequestHeader(value="authToken") String authToken, @RequestBody AddOrderQuantityRequest addOrderQuantityRequest) {
        try {
            LOGGER.info("addOrderQuantityDetails(): Request received to add order quantity details: {} and authToken: {}", toJson(addOrderQuantityRequest), authToken);
            shopAdminOrderOperationsService.addOrderQuantityDetails(authToken, addOrderQuantityRequest);
            return new ResponseEntity<>(new AddOrderQuantityResponse(true), HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }

}
