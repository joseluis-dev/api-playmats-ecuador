package com.playmatsec.app.controller.model;

import java.util.List;

import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.utils.Consts.ResourceType;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResourceDTO {
    private String name;
    private String url;
    private String thumbnail;
    private String watermark;
    private String hosting;
    private List<Product> products;
    private ResourceType type;
    private Boolean isBanner;
}
