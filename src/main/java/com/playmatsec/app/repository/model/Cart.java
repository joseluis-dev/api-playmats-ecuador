package com.playmatsec.app.repository.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.playmatsec.app.controller.model.CartDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinTable(
        name = "cart_products",
        joinColumns = @JoinColumn(name = "cart_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    public void update(CartDTO cart) {
        this.quantity = cart.getQuantity();
        this.price = cart.getPrice();
        this.subtotal = cart.getSubtotal();
        this.updatedAt = LocalDateTime.now();
    }

    @PreRemove
    private void preRemove() {
        if (user != null) user.setCart(null);
        products.clear();
    }
}
