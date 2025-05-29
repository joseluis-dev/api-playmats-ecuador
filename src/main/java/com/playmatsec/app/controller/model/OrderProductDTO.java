package com.playmatsec.app.controller.model;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderProductDTO {
    private UUID id;
    private ProductDTO product;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
