package com.playmatsec.app.controller.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.playmatsec.app.repository.model.User;
import java.util.List;

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
public class CartDTO {
    private User user;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartProductDTO> cartProducts;
}
