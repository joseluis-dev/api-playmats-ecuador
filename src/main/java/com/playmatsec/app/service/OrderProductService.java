package com.playmatsec.app.service;

import java.math.BigDecimal;
import java.util.List;
import com.playmatsec.app.repository.model.OrderProduct;
// import com.playmatsec.app.controller.model.OrderProductDTO;

public interface OrderProductService {
    List<OrderProduct> getOrderProducts(String orderId, String productId, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal, String createdAt);
    OrderProduct getOrderProductById(String id);
    // OrderProduct createOrderProduct(OrderProductDTO orderProduct);
    // OrderProduct updateOrderProduct(String id, String updateRequest);
    // OrderProduct updateOrderProduct(String id, OrderProductDTO orderProduct);
    // Boolean deleteOrderProduct(String id);
}
