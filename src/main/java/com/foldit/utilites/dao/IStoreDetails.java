package com.foldit.utilites.dao;

import com.foldit.utilites.store.model.StoreDetails;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface IStoreDetails extends MongoRepository<StoreDetails, String> {

    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'operatingHourStartTime': 1, 'operatingHourEndTime': 1 }}"
    })
    StoreDetails getShopTimingsFromStoreId(String id);


    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'deliveryFeePerKmAfterThreshold': 1, 'freeDeliveryDistanceAllowed': 1, 'storeLocation': 1}}"
    })
    StoreDetails getShopDeliveryFeeRelatedInformation(String id);

    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'shopWorkerIds': 1, 'shopAdminIds': 1}}"
    })
    StoreDetails getWorkerAndShopAdminIds(String id);

    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'shopRiderIds': 1, 'shopAdminIds': 1}}"
    })
    StoreDetails getRiderAndShopAdminIds(String id);


    @Aggregation(pipeline = {
            "{ $match: { 'shopWorkerIds': { '$in': ?0 } }},",
            "{ $project: { '_id': 1 }}"
    })
    StoreDetails getShopIdWhichWorkerIsPartOf(List<String> workerId);


    @Aggregation(pipeline = {
            "{ $match: { 'shopRiderIds': { '$in': ?0 } }},",
            "{ $project: { '_id': 1 }}"
    })
    StoreDetails getShopIdWhichRiderIsPartOf(List<String> riderId);


    /**
     * Getting ids for people ( Rider, Worker, Admin )
      */

    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'shopAdminIds': 1, 'shopRiderIds': 1, 'shopWorkerIds': 1}}"
    })
    StoreDetails getShopAdminWorkerRiderIds(String id);

    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'shopAdminIds': 1}}"
    })
    StoreDetails getShopAdminIds(String id);

    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'shopRiderIds': 1}}"
    })
    StoreDetails getShopRiderIds(String id);

    @Aggregation(pipeline = {
            "{ $match: { '_id': ?0 }},",
            "{ $project: { 'shopWorkerIds': 1}}"
    })
    StoreDetails getShopWorkerIds(String id);


}
