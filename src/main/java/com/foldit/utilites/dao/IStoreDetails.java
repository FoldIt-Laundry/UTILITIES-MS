package com.foldit.utilites.dao;

import com.foldit.utilites.store.model.StoreDetails;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;


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



}
