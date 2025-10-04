package com.playmatsec.app.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.repository.model.Attribute;
import com.playmatsec.app.controller.model.ProductDTO;
import com.playmatsec.app.controller.model.ResourceUploadDTO;
import com.playmatsec.app.controller.model.ResourceProductRequestDTO;

public interface ProductService {
    List<Product> getProducts(String name, String description, Double price, Boolean isCustomizable, String resourceFilter);
    Product getProductById(String id);
    Product createProduct(ProductDTO product);
    Product updateProduct(String id, String updateRequest);
    Product updateProduct(String id, ProductDTO product);
    Boolean deleteProduct(String id);

    // Category management methods
    List<Category> getProductCategories(String productId);
    Product addCategoriesToProduct(String productId, List<String> categoryIds);
    Product replaceProductCategories(String productId, List<String> categoryIds);

    // Attribute management methods
    List<Attribute> getProductAttributes(String productId);
    Product addAttributesToProduct(String productId, List<String> attributeIds);
    Product replaceProductAttributes(String productId, List<String> attributeIds);

    // Resource management methods
    List<Product.ResourceWithBanner> getProductResources(String productId);
    Product addResourceToProduct(String productId, MultipartFile file, ResourceUploadDTO uploadDTO);
    Product.ResourceWithBanner addResourceToProductAndReturnResource(String productId, MultipartFile file, ResourceUploadDTO uploadDTO);
    Product addResourcesToProduct(String productId, List<String> resourceIds);
    Product replaceProductResources(String productId, List<String> resourceIds);
    Product replaceProductResourcesWithBanner(String productId, List<ResourceProductRequestDTO> resourcesProduct);
    Boolean deleteResourceFromProduct(String productId, String resourceId);
}
