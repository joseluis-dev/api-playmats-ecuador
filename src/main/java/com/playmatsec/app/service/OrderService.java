package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.Order;
import com.playmatsec.app.repository.model.OrderProduct;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.controller.model.OrderDTO;

public interface OrderService {
    List<Order> getOrders(String user, String createdAt, String updatedAt, String status, String totalAmount, String shippingAddress, String billingAddress, String payment);
    Order getOrderById(String id);
    List<OrderProduct> getOrderProductsByOrderId(String orderId);
    List<Product> getProductsByOrderId(String orderId);
    Order createOrder(OrderDTO order);
    Order updateOrder(String id, String updateRequest);
    Order updateOrder(String id, OrderDTO order);
    Boolean deleteOrder(String id);
}
