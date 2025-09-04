package com.playmatsec.app.controller.model;

import java.math.BigDecimal;

import com.playmatsec.app.repository.model.Cart;
import com.playmatsec.app.repository.model.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CartProductDTO {
    private Cart cart;
    private Product product;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private String createdAt;
    private String updatedAt;
}
