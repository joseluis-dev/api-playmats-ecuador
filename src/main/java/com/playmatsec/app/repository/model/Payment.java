package com.playmatsec.app.repository.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.playmatsec.app.controller.model.PaymentDTO;
import com.playmatsec.app.repository.utils.Consts.PaymentMethod;
import com.playmatsec.app.repository.utils.Consts.PaymentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    private UUID id;

    @OneToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference(value = "order-payment")
    private Order order;

    private BigDecimal amount;
    private String providerPaymentId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String imageUrl;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public void update(PaymentDTO payment) {
        this.amount = payment.getAmount();
        this.providerPaymentId = payment.getProviderPaymentId();
        this.method = payment.getMethod();
        this.status = payment.getStatus();
        this.imageUrl = payment.getImageUrl();
        this.paidAt = payment.getPaidAt();
    }

    @PreRemove
    public void preRemove() {
        if (order != null) {
            order.setPayment(null);
        }
    }
}
