package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.controller.model.ProductDTO;

public interface ProductService {
    List<Product> getProducts(String name, String description, Double price, Boolean isCustomizable);
    Product getProductById(String id);
    Product createProduct(ProductDTO product);
    Product updateProduct(String id, String updateRequest);
    Product updateProduct(String id, ProductDTO product);
    Boolean deleteProduct(String id);
}
