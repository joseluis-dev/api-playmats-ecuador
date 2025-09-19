package com.playmatsec.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.playmatsec.app.repository.utils.Consts.ResourceType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.ProductDTO;
import com.playmatsec.app.controller.model.ResourceUploadDTO;
import com.playmatsec.app.controller.model.ResourceProductDTO;
import com.playmatsec.app.repository.ProductRepository;
import com.playmatsec.app.repository.CategoryRepository;
import com.playmatsec.app.repository.AttributeRepository;
import com.playmatsec.app.repository.ResourceRepository;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.repository.model.ResourceProduct;
import com.playmatsec.app.repository.model.Product.ResourceWithBanner;
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
    private final ResourceProductService resourceProductService;
    private final ObjectMapper objectMapper;

    @Override
    public List<Product> getProducts(String name, String description, Double price, Boolean isCustomizable) {
        if (StringUtils.hasLength(name) || StringUtils.hasLength(description) || price != null
                || isCustomizable != null) {
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
                && request.getIsCustomizable() != null) {
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
    public List<Product.ResourceWithBanner> getProductResources(String productId) {
        Product product = getProductById(productId);
        if (product != null) {
            return product.getProductResources();
        }
        return null;
    }

    @Override
    public Product addResourceToProduct(String productId, MultipartFile file, ResourceUploadDTO uploadDTO) {
        try {
            Product product = getProductById(productId);
            if (product != null && file != null && !file.isEmpty()) {
                // Determinar el tipo de archivo automáticamente
                ResourceType detectedType = determineResourceType(file);

                String folder = "products/";
                Map<String, String> uploadResult = cloudinaryService.uploadImage(file, folder);

                if (uploadResult != null) {
                    // Usar nombre personalizado o nombre original del archivo
                    String resourceName = uploadDTO.getName();
                    if (resourceName == null || resourceName.isEmpty()) {
                        resourceName = file.getOriginalFilename();
                    }

                    Resource resource = new Resource();
                    resource.setName(resourceName);
                    resource.setUrl(uploadResult.get("url"));
                    resource.setThumbnail(uploadResult.get("thumbnail"));
                    resource.setWatermark(uploadResult.get("watermark"));
                    resource.setHosting("cloudinary");
                    resource.setType(detectedType);
                    resource.setPublicId(uploadResult.get("publicId"));

                    // Save the resource first
                    Resource savedResource = resourceRepository.save(resource);

                    // Create the ResourceProduct relationship with isBanner property
                    ResourceProductDTO resourceProductDTO = new ResourceProductDTO();
                    resourceProductDTO.setResourceId(savedResource.getId().toString());
                    resourceProductDTO.setProductId(productId);
                    resourceProductDTO.setIsBanner(uploadDTO.getIsBanner());

                    // Create the relationship
                    resourceProductService.createResourceProduct(resourceProductDTO);

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
    public Product.ResourceWithBanner addResourceToProductAndReturnResource(String productId, MultipartFile file, ResourceUploadDTO uploadDTO) {
        try {
            Product product = getProductById(productId);
            if (product != null && file != null && !file.isEmpty()) {
                // Determinar el tipo automáticamente si no viene
                ResourceType detectedType = determineResourceType(file);

                String folder = "products/";
                Map<String, String> uploadResult = cloudinaryService.uploadImage(file, folder);

                if (uploadResult != null) {
                    String resourceName = uploadDTO.getName();
                    if (resourceName == null || resourceName.isEmpty()) {
                        resourceName = file.getOriginalFilename();
                    }

                    Resource resource = new Resource();
                    resource.setName(resourceName);
                    resource.setUrl(uploadResult.get("url"));
                    resource.setThumbnail(uploadResult.get("thumbnail"));
                    resource.setWatermark(uploadResult.get("watermark"));
                    resource.setHosting("cloudinary");
                    resource.setType(detectedType);
                    resource.setPublicId(uploadResult.get("publicId"));

                    Resource savedResource = resourceRepository.save(resource);
                    log.info("*** Resource saved with ID: {}", savedResource.getId());
                    ResourceProductDTO resourceProductDTO = new ResourceProductDTO();
                    resourceProductDTO.setResourceId(savedResource.getId().toString());
                    resourceProductDTO.setProductId(productId);
                    resourceProductDTO.setIsBanner(uploadDTO.getIsBanner());

                    resourceProductService.createResourceProduct(resourceProductDTO);

                    product.setUpdatedAt(LocalDateTime.now());
                    productRepository.save(product);

                    ResourceWithBanner response = new ResourceWithBanner();
                    response.setResource(savedResource);
                    response.setIsBanner(Boolean.TRUE.equals(uploadDTO.getIsBanner()));
                    return response;
                }
            }
        } catch (Exception e) {
            log.error("Error adding resource to product (return resource)", e);
        }
        return null;
    }

    /**
     * Determina automáticamente el tipo de recurso basado en el contenido del
     * archivo
     * 
     * @param file El archivo subido
     * @return El tipo de recurso detectado
     */
    private ResourceType determineResourceType(MultipartFile file) {
        if (file == null) {
            return ResourceType.IMAGE; // Por defecto, si no hay archivo
        }

        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType == null && fileName == null) {
            return ResourceType.IMAGE; // Por defecto, si no hay información
        }

        // Primero intentamos detectar por el contentType
        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase(Locale.ROOT);

            if (lowerContentType.startsWith("image/")) {
                return ResourceType.IMAGE;
            } else if (lowerContentType.startsWith("video/")) {
                return ResourceType.VIDEO;
            } else if (lowerContentType.equals("application/pdf")) {
                return ResourceType.PDF;
            } else if (lowerContentType
                    .equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                    lowerContentType.equals("application/msword")) {
                return ResourceType.DOCX;
            }
        }

        // Si no se pudo detectar por contentType, intentamos por la extensión del
        // nombre
        if (fileName != null) {
            String lowerFileName = fileName.toLowerCase(Locale.ROOT);

            if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") ||
                    lowerFileName.endsWith(".png") || lowerFileName.endsWith(".gif") ||
                    lowerFileName.endsWith(".webp") || lowerFileName.endsWith(".svg")) {
                return ResourceType.IMAGE;
            } else if (lowerFileName.endsWith(".mp4") || lowerFileName.endsWith(".mov") ||
                    lowerFileName.endsWith(".avi") || lowerFileName.endsWith(".webm") ||
                    lowerFileName.endsWith(".mkv")) {
                return ResourceType.VIDEO;
            } else if (lowerFileName.endsWith(".pdf")) {
                return ResourceType.PDF;
            } else if (lowerFileName.endsWith(".docx") || lowerFileName.endsWith(".doc")) {
                return ResourceType.DOCX;
            }
        }

        // Si no se pudo determinar, por defecto usamos IMAGE
        return ResourceType.IMAGE;
    }

    @Override
    public Product addResourcesToProduct(String productId, List<String> resourceIds) {
        try {
            Product product = getProductById(productId);
            if (product != null && resourceIds != null && !resourceIds.isEmpty()) {
                for (String resourceId : resourceIds) {
                    // Check if the resource exists
                    Resource resource = resourceRepository.getById(Integer.valueOf(resourceId));

                    // Check if the relationship already exists
                    ResourceProduct existingRelationship = resourceProductService
                            .getResourceProductByResourceIdAndProductId(resourceId, productId);

                    // If resource exists and relationship doesn't exist yet, create it
                    if (resource != null && existingRelationship == null) {
                        ResourceProductDTO resourceProductDTO = new ResourceProductDTO();
                        resourceProductDTO.setResourceId(resourceId);
                        resourceProductDTO.setProductId(productId);
                        resourceProductDTO.setIsBanner(false); // Default value for isBanner

                        resourceProductService.createResourceProduct(resourceProductDTO);
                    }
                }

                product.setUpdatedAt(LocalDateTime.now());
                return productRepository.save(product);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid resource ID format in the list", e);
        }
        return null;
    }

    @Override
    @Transactional
    public Product replaceProductResources(String productId, List<String> resourceIds) {
        try {
            Product product = getProductById(productId);
            if (product == null) {
                return null;
            }

            // If list is null, do nothing (preserve current relationships)
            if (resourceIds == null) {
                return product;
            }

            // Normalize desired IDs (trim, dedupe, skip blanks)
            Set<String> desiredIds = new HashSet<>();
            for (String rid : resourceIds) {
                if (rid != null) {
                    String trimmed = rid.trim();
                    if (!trimmed.isEmpty()) {
                        desiredIds.add(trimmed);
                    }
                }
            }

            // Load current relationships and build current ID set
            List<ResourceProduct> currentRelationships = resourceProductService
                    .getResourceProductsByProductId(productId);
            Set<String> currentIds = new HashSet<>();
            if (currentRelationships != null) {
                for (ResourceProduct rp : currentRelationships) {
                    try {
                        Resource res = rp.getResource();
                        if (res != null && res.getId() != null) {
                            currentIds.add(res.getId().toString());
                        }
                    } catch (Exception ex) {
                        // Be tolerant if lazy loading or mapping issues arise
                        log.debug("Could not resolve resource id from ResourceProduct {}", rp != null ? rp.getId() : null, ex);
                    }
                }
            }

            boolean changed = false;

            // Determine which to add (desired - current)
            for (String desiredId : desiredIds) {
                if (!currentIds.contains(desiredId)) {
                    // Validate resource exists
                    try {
                        Resource resource = resourceRepository.getById(Integer.valueOf(desiredId));
                        if (resource != null) {
                            ResourceProductDTO dto = new ResourceProductDTO();
                            dto.setResourceId(desiredId);
                            dto.setProductId(productId);
                            dto.setIsBanner(false); // default flag for new relations
                            resourceProductService.createResourceProduct(dto);
                            changed = true;
                        }
                    } catch (IllegalArgumentException iae) {
                        log.warn("Skipping invalid resource id '{}' while adding to product {}", desiredId, productId);
                    }
                }
            }

            // Determine which to remove (current - desired)
            for (String currentId : currentIds) {
                if (!desiredIds.contains(currentId)) {
                    // Remove relationship only (do not delete the resource entity here)
                    boolean removed = resourceProductService
                            .deleteResourceProductByResourceIdAndProductId(currentId, productId);
                    if (!removed) {
                        log.warn("Failed to remove relation: product {} - resource {}", productId, currentId);
                    }
                    changed = true;
                }
            }

            if (changed) {
                product.setUpdatedAt(LocalDateTime.now());
                return productRepository.save(product);
            }

            // No changes required
            return product;
        } catch (Exception e) {
            log.error("Error replacing product resources for product {}", productId, e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public Boolean deleteResourceFromProduct(String productId, String resourceId) {
        try {
            // Validar que el producto exista
            Product product = getProductById(productId);
            if (product == null) {
                log.warn("Producto no encontrado con ID: {}", productId);
                return false;
            }
            
            // Validar que el recurso exista
            Integer rid = Integer.parseInt(resourceId);
            Resource resource = resourceRepository.getById(rid);
            if (resource == null) {
                log.warn("Recurso no encontrado con ID: {}", resourceId);
                return false;
            }

            // Regla especial: si el recurso pertenece a la categoría "Sellos",
            // solo se elimina la relación con el producto, sin borrar el recurso
            boolean isSellosCategory = false;
            try {
                if (resource.getCategories() != null) {
                    isSellosCategory = resource.getCategories().stream()
                        .anyMatch(cat -> cat != null && cat.getName() != null && cat.getName().equalsIgnoreCase("Sellos"));
                }
            } catch (Exception e) {
                log.warn("No se pudo evaluar categorías del recurso {} para la regla 'Sellos'", resourceId, e);
            }
            
            // Verificar si existe la relación entre producto y recurso
            ResourceProduct resourceProduct = resourceProductService.getResourceProductByResourceIdAndProductId(resourceId, productId);
            if (resourceProduct == null) {
                log.warn("No existe relación entre el producto {} y el recurso {}", productId, resourceId);
                return false;
            }
            
            // Verificar si el recurso está asociado solo a este producto
            List<ResourceProduct> allRelationships = resourceProductService.getResourceProductsByResourceId(resourceId);
            boolean isOnlyAssociatedWithThisProduct = (allRelationships.size() == 1);
            
            // Eliminar la relación entre el producto y el recurso
            boolean relationshipDeleted = resourceProductService.deleteResourceProductByResourceIdAndProductId(resourceId, productId);
            
            if (!relationshipDeleted) {
                log.error("Error al eliminar la relación entre el producto {} y el recurso {}", productId, resourceId);
                return false;
            }
            
            // Si NO es un recurso de la categoría "Sellos" y
            // solo está asociado a este producto, eliminarlo también
            if (!isSellosCategory && isOnlyAssociatedWithThisProduct) {
                String publicId = resource.getPublicId();
                
                // Primero eliminar el registro de la base de datos
                resourceRepository.delete(resource);
                
                // Solo si se eliminó exitosamente de la base de datos, eliminar de Cloudinary
                if (publicId != null && !publicId.isEmpty()) {
                    boolean cloudinaryDeleted = cloudinaryService.deleteImage(publicId);
                    if (!cloudinaryDeleted) {
                        log.warn("No se pudo eliminar la imagen de Cloudinary con publicId: {}", publicId);
                    }
                }
                
                log.info("Recurso {} eliminado completamente (base de datos y Cloudinary) ya que solo estaba asociado al producto {}", resourceId, productId);
            } else {
                if (isSellosCategory) {
                    log.info("Recurso {} pertenece a la categoría 'Sellos'. Se conserva el recurso y solo se elimina la relación con el producto {}", resourceId, productId);
                } else {
                    log.info("Solo se eliminó la relación entre el producto {} y el recurso {} ya que el recurso está asociado a otros productos", productId, resourceId);
                }
            }
            
            // Actualizar la fecha de actualización del producto
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            
            return true;
            
        } catch (NumberFormatException e) {
            log.error("ID de recurso inválido: {}", resourceId, e);
        } catch (IllegalArgumentException e) {
            log.error("ID de producto inválido: {}", productId, e);
        } catch (Exception e) {
            log.error("Error al eliminar el recurso {} del producto {}", resourceId, productId, e);
        }
        
        return false;
    }
}