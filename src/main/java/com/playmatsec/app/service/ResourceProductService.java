package com.playmatsec.app.service;

import java.util.List;

import com.playmatsec.app.repository.model.ResourceProduct;
import com.playmatsec.app.controller.model.ResourceProductDTO;

public interface ResourceProductService {
    List<ResourceProduct> getResourceProducts();
    ResourceProduct getResourceProductById(String id);
    ResourceProduct createResourceProduct(ResourceProductDTO resourceProduct);
    ResourceProduct updateResourceProduct(String id, ResourceProductDTO resourceProduct);
    Boolean deleteResourceProduct(String id);
    
    List<ResourceProduct> getResourceProductsByProductId(String productId);
    List<ResourceProduct> getResourceProductsByResourceId(String resourceId);
    ResourceProduct getResourceProductByResourceIdAndProductId(String resourceId, String productId);
    Boolean deleteResourceProductByResourceIdAndProductId(String resourceId, String productId);
    
    List<ResourceProduct> getBannersByProductId(String productId);
}
