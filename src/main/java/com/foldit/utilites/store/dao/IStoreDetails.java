package com.foldit.utilites.store.dao;

import com.foldit.utilites.store.model.StoreDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IStoreDetails extends MongoRepository<StoreDetails, String> {



}
