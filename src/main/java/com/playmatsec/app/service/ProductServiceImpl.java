package com.playmatsec.app.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.ProductDTO;
import com.playmatsec.app.repository.ProductRepository;
import com.playmatsec.app.repository.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Product> getProducts(String name, String description, Double price, Boolean isCustomizable) {
        if (StringUtils.hasLength(name) || StringUtils.hasLength(description) || price != null || isCustomizable != null) {
            return productRepository.search(name, description, price, isCustomizable);
        }
        List<Product> products = productRepository.getProducts();
        return products.isEmpty() ? null : products;
    }

    @Override
    public Product getProductById(String id) {
        try {
            UUID productId = UUID.fromString(id);
            return productRepository.getById(productId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid product ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public Product createProduct(ProductDTO request) {
        if (request != null && StringUtils.hasLength(request.getName())) {
            Product product = objectMapper.convertValue(request, Product.class);
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public Product updateProduct(String id, String request) {
        Product product = getProductById(id);
        if (product != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(product)));
                Product patched = objectMapper.treeToValue(target, Product.class);
                productRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating product {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Product updateProduct(String id, ProductDTO request) {
        Product product = getProductById(id);
        if (product != null) {
            // product.update(request); // Implementar si existe método update
            productRepository.save(product);
            return product;
        }
        return null;
    }

    @Override
    public Boolean deleteProduct(String id) {
        try {
            UUID productId = UUID.fromString(id);
            Product product = productRepository.getById(productId);
            if (product != null) {
                productRepository.delete(product);
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid product ID format: {}", id, e);
        }
        return false;
    }
}
