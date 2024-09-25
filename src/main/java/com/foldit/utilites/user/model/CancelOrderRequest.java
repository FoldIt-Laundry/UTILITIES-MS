package com.foldit.utilites.user.model;

import com.foldit.utilites.order.model.OrderDetails;

public record CancelOrderRequest(String userId,
                                 OrderDetails orderDetails) {
}
