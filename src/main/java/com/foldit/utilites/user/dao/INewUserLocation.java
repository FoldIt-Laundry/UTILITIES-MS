package com.foldit.utilites.user.dao;

import com.foldit.utilites.user.model.UserLocation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface INewUserLocation extends MongoRepository<UserLocation,String> {

    @Query("{ 'id': :#{#userId} }")
    void addItemToList(@Param("userId") String userId, @Param("locations") UserLocation userLocation);

}
