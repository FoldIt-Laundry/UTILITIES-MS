package com.foldit.utilites.store.dao;

import com.foldit.utilites.store.model.StoreDetails;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface IStoreDetails extends MongoRepository<StoreDetails, String> {

    @Aggregation(pipeline = {
                    "{ location:" +
                    "       { $near:" +
                    "          {" +
                    "            $geometry: { type: 'Point',  coordinates: [ -73.9667, 40.78 ] }," +
                    "            $minDistance: 1000," +
                    "            $maxDistance: 5000" +
                    "          }" +
                    "       }" +
                    "   }"
    })
    Object getNearestAvailableStores(Double[] coordinates);



}
