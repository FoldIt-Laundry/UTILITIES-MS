package com.foldit.utilites.dao;

import com.foldit.utilites.order.model.BasicOrderDetails;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.order.model.WorkflowStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IOrderDetails extends MongoRepository<OrderDetails, String> {

    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0 }}"
    })
    List<OrderDetails> getAllOrdersListFromUserId(String userId);

    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0, 'userWorkflowStatus': {'$nin':  ?1} }}"
    })
    List<OrderDetails> getAllActiveOrdersListFromUserId(String userId, List<WorkflowStatus> workflowStatus);

    @Aggregation(pipeline = {
            "{ $match: { '_id': {'$in':  ?0} }}",
            "{ $project: { 'userId': 1 }}"
    })
    List<OrderDetails> getAllUserIdFromGivenOrderIdList(List<String> orderIdList);

    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0 }},",
            "{ $project: { 'orderDeliveryTimeStamp': 1, 'userAddress': 1, 'shopAddress': 1, 'billDetails.finalPrice': 1, '_id': 1, 'addedService':  1 }}"
    })
    List<BasicOrderDetails> getBasicOrderDetailsFromUserId(String userId);


    @Aggregation(pipeline = {
            "{ $match: { 'storeId': ?0, 'workerRiderWorkflowStatus': ?1 }}",
    })
    List<OrderDetails> getAllUnApprovedOrderList(String storeId, String workflowStatus);

    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'userId': 1 }}"
    })
    OrderDetails getUserIdFromOrderId(String orderId);

    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'userId': 1, 'checkOutOtp': 1 }}"
    })
    OrderDetails getUserIdAndOtpFromOrderId(String orderId);

    @Aggregation(pipeline = {
            "{ $match: { 'workerRiderWorkflowStatus': ?1 }}",
    })
    List<OrderDetails> getAllPickUpOrderDetailsFromRiderId(String workflowStatus);


}
