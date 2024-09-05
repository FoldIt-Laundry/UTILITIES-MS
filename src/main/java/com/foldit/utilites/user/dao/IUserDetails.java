package com.foldit.utilites.user.dao;

import com.foldit.utilites.order.model.MetaDataOrderDetailsInUserCollection;
import com.foldit.utilites.user.model.UserDetails;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface IUserDetails extends MongoRepository<UserDetails, String> {

    @Aggregation(pipeline = {
            "{ $match: { '_id': ObjectId(?0) } }",        // Match by id (or userId)
            "{ $project: { 'orderHistory': 1, '_id': 0 } }"  // Project only the orderHistory field, exclude _id
    })
    List<MetaDataOrderDetailsInUserCollection> findUserOrderDetailsByUserId(String userId);

}
