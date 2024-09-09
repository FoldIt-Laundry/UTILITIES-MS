package com.foldit.utilites.order.dao;

import com.foldit.utilites.order.model.BasicOrderDetails;
import com.foldit.utilites.order.model.OrderDetails;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IOrderDetails extends MongoRepository<OrderDetails, String> {

    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0 }},"
    })
    List<OrderDetails> getAllOrdersListFromUserId(String userId);

    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0, 'workflowStatus': ?1 }},"
    })
    List<OrderDetails> getAllActiveOrdersListFromUserId(String userId, String workflowStatus);

    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0 }},",
            "{ $project: { 'orderDeliveryTimeStamp': 1, 'userAddress': 1, 'shopAddress': 1, 'billDetails.finalPrice': 1, '_id': 1, 'addedService':  1 }}"
    })
    List<BasicOrderDetails> getBasicOrderDetailsFromUserId(String userId);

}
