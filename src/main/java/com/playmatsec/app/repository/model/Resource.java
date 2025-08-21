package com.playmatsec.app.repository.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.playmatsec.app.controller.model.ResourceDTO;
import com.playmatsec.app.repository.utils.Consts.ResourceType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resources")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String publicId;

    private String name;
    private String url;
    private String thumbnail;
    private String watermark;
    private String hosting;

    @OneToMany(mappedBy = "resource")
    @JsonIgnore
    private List<ResourceProduct> resourceProducts;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    @ManyToMany
    @JoinTable(
        name = "resource_categories",
        joinColumns = {@JoinColumn(name = "resource_id")},
        inverseJoinColumns = {@JoinColumn(name = "category_id")}
    )
    private List<Category> categories;
    @ManyToMany
    @JoinTable(
        name = "resource_attributes",
        joinColumns = {@JoinColumn(name = "resource_id")},
        inverseJoinColumns = {@JoinColumn(name = "attribute_id")}
    )
    private List<Attribute> attributes;

    public void update(ResourceDTO resource) {
        this.publicId = resource.getPublicId();
        this.name = resource.getName();
        this.url = resource.getUrl();
        this.thumbnail = resource.getThumbnail();
        this.watermark = resource.getWatermark();
        this.hosting = resource.getHosting();
        this.type = resource.getType();
        // isBanner se maneja ahora en ResourceProduct
    }
}
