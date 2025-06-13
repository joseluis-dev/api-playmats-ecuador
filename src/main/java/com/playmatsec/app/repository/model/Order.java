package com.playmatsec.app.repository.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.playmatsec.app.controller.model.OrderDTO;
import com.playmatsec.app.repository.utils.Consts.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-orders")
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "shipping_address_id")
    private ShippingAddress shippingAddress;

    private String billingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JsonManagedReference(value = "order-orderProducts")
    private List<OrderProduct> orderProducts;

    @OneToOne(mappedBy = "order")
    @JsonManagedReference(value = "order-payment")
    private Payment payment;

    public void update(OrderDTO order) {
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.shippingAddress = order.getShippingAddress();
        this.billingAddress = order.getBillingAddress();
        this.updatedAt = LocalDateTime.now();
    }
}