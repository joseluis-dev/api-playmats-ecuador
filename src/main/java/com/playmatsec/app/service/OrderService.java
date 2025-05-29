package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.Order;
import com.playmatsec.app.controller.model.OrderDTO;

public interface OrderService {
    List<Order> getOrders(String userId, String createdAt, String updatedAt, String status);
    Order getOrderById(String id);
    Order createOrder(OrderDTO order);
    Order updateOrder(String id, String updateRequest);
    Order updateOrder(String id, OrderDTO order);
    Boolean deleteOrder(String id);
}
