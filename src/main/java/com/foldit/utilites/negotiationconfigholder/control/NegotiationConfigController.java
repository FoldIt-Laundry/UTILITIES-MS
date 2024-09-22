package com.foldit.utilites.negotiationconfigholder.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.negotiationconfigholder.model.ChangeBatchSlotTimingRequest;
import com.foldit.utilites.negotiationconfigholder.service.NegotiationConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@RestController
public class NegotiationConfigController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(NegotiationConfigController.class);

    @Autowired
    private NegotiationConfigService negotiationConfigService;

    @PutMapping("/config/changeBatchSlotTimings")
    public ResponseEntity<Boolean> changeBatchSlotTimings(@RequestHeader(value="authToken") String authToken, @RequestHeader(value="role") String userRole, @RequestBody ChangeBatchSlotTimingRequest changeBatchSlotTimingRequest) {
        try {
            LOGGER.info("changeBatchSlotTimings(): Request received to change the slots quantity to show for authToken: {}, role: {} and requestDetails: {}", authToken, userRole, toJson(changeBatchSlotTimingRequest));
            negotiationConfigService.changeBatchSlotTimings(authToken, userRole, changeBatchSlotTimingRequest);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("changeBatchSlotTimings(): Exception occurred while updating the batch slot timings details for request: {} , Exception: {}", toJson(changeBatchSlotTimingRequest), ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/config/changeSlotsQuantityToShow")
    public ResponseEntity<Boolean> changeSlotsQuantityToShow(@RequestHeader(value="authToken") String authToken, @RequestHeader(value="role") String userRole, @RequestParam("slotsQuantity") Integer slotsQuantity,@RequestParam("userId") String userId) {
        try {
            LOGGER.info("changeSlotsQuantityToShow(): Request received to change the slots quantity to show for authToken: {}, role: {}, slotsQuantity: {} and userId: {}", authToken, userRole, slotsQuantity, userId);
            negotiationConfigService.changeSlotsQuantityToShow(authToken, userRole, slotsQuantity, userId);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("changeSlotsQuantityToShow(): Exception occurred while changeSlotsQuantityToShow for userId: {} and slotsQuantity: {}, Exception: {}", userId, slotsQuantity ,ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

}
