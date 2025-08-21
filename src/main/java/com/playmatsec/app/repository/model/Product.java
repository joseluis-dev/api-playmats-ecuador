package com.playmatsec.app.repository.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.playmatsec.app.controller.model.ProductDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    private UUID id;

    private String name;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isCustomizable;

    @OneToMany(mappedBy = "product")
    private List<ResourceProduct> resourceProducts;
    @ManyToMany
    @JoinTable(
        name = "product_categories",
        joinColumns = {@JoinColumn(name = "product_id")},
        inverseJoinColumns = {@JoinColumn(name = "category_id")}
    )
    private List<Category> categories;
    @ManyToMany
    @JoinTable(
        name = "product_attributes",
        joinColumns = {@JoinColumn(name = "product_id")},
        inverseJoinColumns = {@JoinColumn(name = "attribute_id")}
    )
    private List<Attribute> attributes;
    @ManyToMany(mappedBy = "products")
    @JsonIgnore
    private List<Cart> carts;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<OrderProduct> orderProducts;

    public void update(ProductDTO product) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.isCustomizable = product.getIsCustomizable();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Método para obtener los recursos asociados al producto con información de banner
    public List<ResourceWithBanner> getProductResources() {
        if (resourceProducts == null || resourceProducts.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        return resourceProducts.stream()
            .map(rp -> new ResourceWithBanner(rp.getResource(), rp.getIsBanner()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    // Clase interna para representar un recurso con su bandera isBanner
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResourceWithBanner {
        private Resource resource;
        private Boolean isBanner;
    }
}
