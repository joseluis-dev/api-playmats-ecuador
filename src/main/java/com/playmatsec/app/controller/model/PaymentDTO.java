package com.playmatsec.app.controller.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.playmatsec.app.repository.model.Order;
import com.playmatsec.app.repository.utils.Consts.PaymentMethod;
import com.playmatsec.app.repository.utils.Consts.PaymentStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PaymentDTO {
    private Order order;
    private BigDecimal amount;
    private String providerPaymentId;
    private PaymentMethod method;
    private PaymentStatus status;
    private String imageUrl;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
