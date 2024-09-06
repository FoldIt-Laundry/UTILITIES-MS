package com.foldit.utilites.store.dao;

import com.foldit.utilites.store.model.StoreDetails;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IStoreDetails extends MongoRepository<StoreDetails, String> {

    @Aggregation(pipeline = {
            "{\n" +
                    "    $geoNear: {\n" +
                    "      near: { type: \"Point\", coordinates: [ -73.9667, 40.78 ] },\n" +
                    "      distanceField: \"distance\",\n" +
                    "      minDistance: 1000,\n" +
                    "      maxDistance: 5000,\n" +
                    "      spherical: true\n" +
                    "    }\n" +
                    "  }"
//                    "       { $geoNear:" +
//                    "          {" +
//                    "            $geometry: { type: 'Point',  coordinates: ?0 }," +
//                    "            $maxDistance: 5000" +
//                    "          }" +
//                    "       }"
    })
    Object getNearestAvailableStores(Double[] coordinates);

}
