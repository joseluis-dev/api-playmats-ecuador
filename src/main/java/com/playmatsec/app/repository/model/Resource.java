package com.playmatsec.app.repository.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.playmatsec.app.controller.model.ResourceDTO;
import com.playmatsec.app.repository.utils.Consts.ResourceType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String url;
    private String thumbnail;
    private String watermark;
    private String hosting;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference(value = "product-resources")
    private Product product;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    private Boolean isBanner;

    public void update(ResourceDTO resource) {
        this.name = resource.getName();
        this.url = resource.getUrl();
        this.thumbnail = resource.getThumbnail();
        this.watermark = resource.getWatermark();
        this.hosting = resource.getHosting();
        this.type = resource.getType();
        this.isBanner = resource.getIsBanner();
    }
}
