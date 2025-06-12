package com.playmatsec.app.controller.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.playmatsec.app.repository.model.OrderProduct;
import com.playmatsec.app.repository.model.Payment;
import com.playmatsec.app.repository.model.ShippingAddress;
import com.playmatsec.app.repository.model.User;
import com.playmatsec.app.repository.utils.Consts.OrderStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderDTO {
    private User user;
    private Payment payment;
    private ShippingAddress shippingAddress;
    private String billingAddress;
    private List<OrderProduct> orderProducts;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
