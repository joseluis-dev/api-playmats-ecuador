package com.playmatsec.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.ProductDTO;
import com.playmatsec.app.controller.model.ResourceUploadDTO;
import com.playmatsec.app.repository.ProductRepository;
import com.playmatsec.app.repository.CategoryRepository;
import com.playmatsec.app.repository.AttributeRepository;
import com.playmatsec.app.repository.ResourceRepository;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.repository.model.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeRepository attributeRepository;
    private final ResourceRepository resourceRepository;
    private final CloudinaryService cloudinaryService;
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
        if (request != null
            && StringUtils.hasLength(request.getName())
            && StringUtils.hasLength(request.getDescription())
            && request.getPrice() != null
            && request.getIsCustomizable() != null
            ) {
            Product product = objectMapper.convertValue(request, Product.class);
            product.setId(UUID.randomUUID());
            product.setCreatedAt(LocalDateTime.now());
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
                patched.setUpdatedAt(LocalDateTime.now());
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
            product.update(request);
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

    @Override
    public List<Category> getProductCategories(String productId) {
        Product product = getProductById(productId);
        if (product != null) {
            return product.getCategories();
        }
        return null;
    }

    @Override
    public Product addCategoriesToProduct(String productId, List<String> categoryIds) {
        try {
            Product product = getProductById(productId);
            if (product != null && categoryIds != null && !categoryIds.isEmpty()) {
                List<Category> existingCategories = product.getCategories();
                for (String categoryId : categoryIds) {
                    Category category = categoryRepository.getById(Integer.valueOf(categoryId));
                    if (category != null && !existingCategories.contains(category)) {
                        existingCategories.add(category);
                    }
                }
                product.setCategories(existingCategories);
                product.setUpdatedAt(LocalDateTime.now());
                return productRepository.save(product);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid category ID format in the list", e);
        }
        return null;
    }

    @Override
    public Product replaceProductCategories(String productId, List<String> categoryIds) {
        try {
            Product product = getProductById(productId);
            if (product != null && categoryIds != null) {
                List<Category> newCategories = new ArrayList<>();
                for (String categoryId : categoryIds) {
                    Category category = categoryRepository.getById(Integer.valueOf(categoryId));
                    if (category != null) {
                        newCategories.add(category);
                    }
                }
                product.setCategories(newCategories);
                product.setUpdatedAt(LocalDateTime.now());
                return productRepository.save(product);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid category ID format in the list", e);
        }
        return null;
    }

    @Override
    public List<Attribute> getProductAttributes(String productId) {
        Product product = getProductById(productId);
        if (product != null) {
            return product.getAttributes();
        }
        return null;
    }

    @Override
    public Product addAttributesToProduct(String productId, List<String> attributeIds) {
        try {
            Product product = getProductById(productId);
            if (product != null && attributeIds != null && !attributeIds.isEmpty()) {
                List<Attribute> existingAttributes = product.getAttributes();
                for (String attributeId : attributeIds) {
                    Attribute attribute = attributeRepository.getById(Long.valueOf(attributeId));
                    if (attribute != null && !existingAttributes.contains(attribute)) {
                        existingAttributes.add(attribute);
                    }
                }
                product.setAttributes(existingAttributes);
                product.setUpdatedAt(LocalDateTime.now());
                return productRepository.save(product);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid attribute ID format in the list", e);
        }
        return null;
    }

    @Override
    public Product replaceProductAttributes(String productId, List<String> attributeIds) {
        try {
            Product product = getProductById(productId);
            if (product != null && attributeIds != null) {
                List<Attribute> newAttributes = new ArrayList<>();
                for (String attributeId : attributeIds) {
                    Attribute attribute = attributeRepository.getById(Long.valueOf(attributeId));
                    if (attribute != null) {
                        newAttributes.add(attribute);
                    }
                }
                product.setAttributes(newAttributes);
                product.setUpdatedAt(LocalDateTime.now());
                return productRepository.save(product);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid attribute ID format in the list", e);
        }
        return null;
    }

    @Override
    public List<com.playmatsec.app.repository.model.Resource> getProductResources(String productId) {
        Product product = getProductById(productId);
        if (product != null) {
            return product.getResources();
        }
        return null;
    }

    @Override
    public Product addResourceToProduct(String productId, MultipartFile file, ResourceUploadDTO uploadDTO) {
        try {
            Product product = getProductById(productId);
            if (product != null && file != null && !file.isEmpty()) {
                String folder = "products/" + productId;
                Map<String, String> uploadResult = cloudinaryService.uploadImage(file, folder);
                
                if (uploadResult != null) {
                    Resource resource = new Resource();
                    resource.setName(file.getOriginalFilename());
                    resource.setUrl(uploadResult.get("url"));
                    resource.setThumbnail(uploadResult.get("thumbnail"));
                    resource.setWatermark(uploadResult.get("watermark"));
                    resource.setHosting("cloudinary");
                    resource.setType(uploadDTO.getType());
                    resource.setIsBanner(uploadDTO.getIsBanner());
                    resource.setPublicId(uploadResult.get("publicId"));
                    
                    List<Product> products = new ArrayList<>();
                    products.add(product);
                    resource.setProducts(products);
                    
                    Resource savedResource = resourceRepository.save(resource);
                    List<Resource> resources = product.getResources();
                    resources.add(savedResource);
                    product.setResources(resources);
                    product.setUpdatedAt(LocalDateTime.now());
                    
                    return productRepository.save(product);
                }
            }
        } catch (Exception e) {
            log.error("Error adding resource to product", e);
        }
        return null;
    }

    @Override
    public Product addResourcesToProduct(String productId, List<String> resourceIds) {
        try {
            Product product = getProductById(productId);
            if (product != null && resourceIds != null && !resourceIds.isEmpty()) {
                List<Resource> existingResources = product.getResources();
                for (String resourceId : resourceIds) {
                    Resource resource = resourceRepository.getById(Integer.valueOf(resourceId));
                    if (resource != null && !existingResources.contains(resource)) {
                        existingResources.add(resource);
                    }
                }
                product.setResources(existingResources);
                product.setUpdatedAt(LocalDateTime.now());
                return productRepository.save(product);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid resource ID format in the list", e);
        }
        return null;
    }

    @Override
    public Product replaceProductResources(String productId, List<String> resourceIds) {
        try {
            Product product = getProductById(productId);
            if (product != null && resourceIds != null) {
                List<Resource> newResources = new ArrayList<>();
                for (String resourceId : resourceIds) {
                    Resource resource = resourceRepository.getById(Integer.valueOf(resourceId));
                    if (resource != null) {
                        newResources.add(resource);
                    }
                }
                product.setResources(newResources);
                product.setUpdatedAt(LocalDateTime.now());
                return productRepository.save(product);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid resource ID format in the list", e);
        }
        return null;
    }
}