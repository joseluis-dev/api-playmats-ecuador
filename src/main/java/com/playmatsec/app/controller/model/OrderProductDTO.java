package com.playmatsec.app.controller.model;

import lombok.*;
import java.math.BigDecimal;

import com.playmatsec.app.repository.model.Order;
import com.playmatsec.app.repository.model.Product;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderProductDTO {
    private Order order;
    private Product product;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String createdAt;
    private String updatedAt;
}
