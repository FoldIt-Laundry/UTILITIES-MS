package com.foldit.utilites.order.dao;

import com.foldit.utilites.order.model.OrderDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IOrderDetails extends MongoRepository<OrderDetails, String> {
}
