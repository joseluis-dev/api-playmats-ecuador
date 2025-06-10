package com.playmatsec.app.controller.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProductDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isCustomizable;
}
