package com.playmatsec.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.repository.utils.Consts.ResourceType;
import com.playmatsec.app.controller.model.ResourceUploadDTO;
import com.playmatsec.app.repository.ProductRepository;
import com.playmatsec.app.repository.ResourceRepository;
import com.playmatsec.app.repository.CategoryRepository;
import com.playmatsec.app.repository.AttributeRepository;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.repository.model.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeRepository attributeRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    @Override
    public List<Resource> getResources(String name, String url, String hosting, String thumbnail, String watermark, String type, Boolean isBanner, String product) {
        if (StringUtils.hasLength(name) || StringUtils.hasLength(url) || StringUtils.hasLength(hosting) || StringUtils.hasLength(thumbnail) || StringUtils.hasLength(watermark) || StringUtils.hasLength(type) || isBanner != null || StringUtils.hasLength(product)) {
            return resourceRepository.search(name, url, hosting, thumbnail, watermark, type, isBanner, product);
        }
        List<Resource> resources = resourceRepository.getResources();
        return resources.isEmpty() ? null : resources;
    }

    @Override
    public Resource getResourceById(String id) {
        try {
            Integer resourceId = Integer.parseInt(id);
            return resourceRepository.getById(resourceId);
        } catch (NumberFormatException e) {
            log.error("Invalid resource ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public Resource createResource(MultipartFile file, ResourceUploadDTO uploadDTO) {
        if (file != null && !file.isEmpty()) {
            try {
                // Determinar el tipo de archivo automáticamente
                ResourceType detectedType = determineResourceType(file);
                
                String folder = "resources/";
                Map<String, String> uploadResult = cloudinaryService.uploadImage(file, folder);
                
                if (uploadResult != null) {
                    Resource resource = new Resource();
                    
                    // Usar nombre personalizado o nombre original del archivo
                    String resourceName = uploadDTO.getName();
                    if (resourceName == null || resourceName.isEmpty()) {
                        resourceName = file.getOriginalFilename();
                    }
                    
                    resource.setName(resourceName);
                    resource.setUrl(uploadResult.get("url"));
                    resource.setThumbnail(uploadResult.get("thumbnail"));
                    resource.setWatermark(uploadResult.get("watermark"));
                    resource.setHosting("cloudinary");
                    resource.setType(detectedType);
                    resource.setIsBanner(uploadDTO.getIsBanner());
                    resource.setPublicId(uploadResult.get("publicId"));

                    return resourceRepository.save(resource);
                }
            } catch (Exception e) {
                log.error("Error creating resource", e);
            }
        }
        return null;
    }

    @Override
    public Resource updateResource(String id, String request) {
        Resource resource = getResourceById(id);
        if (resource != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(resource)));
                Resource patched = objectMapper.treeToValue(target, Resource.class);
                if (patched.getProducts() == null) {
                    patched.setProducts(resource.getProducts());
                } else {
                    List<Product> updatedProducts = new ArrayList<>();
                    for (Product pDTO : patched.getProducts()) {
                        if (pDTO.getId() == null) {
                            throw new IllegalArgumentException("Cada product debe incluir id");
                        }
                        Product product = productRepository.getById(pDTO.getId());
                        if (product == null) {
                            throw new IllegalArgumentException("Product no encontrado: " + pDTO.getId());
                        }
                        updatedProducts.add(product);
                    }
                    patched.setProducts(updatedProducts);
                }
                resourceRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating resource {}", id, e);
                return null;
            }
        }
        return null;
    }

    // @Override
    // public Resource updateResource(String id, ResourceDTO request) {
    //     Resource resource = getResourceById(id);
    //     if (resource != null) {
    //         resource.update(request);
    //         if (request.getProducts() != null && !request.getProducts().isEmpty()) {
    //             resource.setProducts(request.getProducts());
    //         }
    //         resourceRepository.save(resource);
    //         return resource;
    //     }
    //     return null;
    // }

    @Override
    public Resource updateResourceWithFile(String id, MultipartFile file, ResourceUploadDTO uploadDTO) {
        Resource resource = getResourceById(id);
        if (resource != null && file != null && !file.isEmpty()) {
            try {
                // Guardar el publicId para eliminarlo después de subir la nueva imagen
                String oldPublicId = resource.getPublicId();
                
                // Determinar el tipo de archivo automáticamente
                ResourceType detectedType = determineResourceType(file);
                
                // Subir la nueva imagen a Cloudinary
                String folder = "resources/";
                Map<String, String> uploadResult = cloudinaryService.uploadImage(file, folder);
                
                if (uploadResult != null) {
                    // Si la carga fue exitosa, eliminar la imagen anterior
                    if (oldPublicId != null && !oldPublicId.isEmpty()) {
                        boolean deleted = cloudinaryService.deleteImage(oldPublicId);
                        if (!deleted) {
                            log.warn("No se pudo eliminar la imagen anterior de Cloudinary: {}", oldPublicId);
                        }
                    }
                    
                    // Mantener el nombre actual si uploadDTO.getName() es null
                    String resourceName = uploadDTO.getName();
                    if (resourceName == null || resourceName.isEmpty()) {
                        // Si el recurso ya tiene un nombre, lo mantenemos
                        // Si no, usamos el nombre del archivo
                        resourceName = (resource.getName() != null && !resource.getName().isEmpty()) 
                            ? resource.getName() 
                            : file.getOriginalFilename();
                    }
                    
                    // Actualizar los campos del recurso
                    resource.setName(resourceName);
                    resource.setUrl(uploadResult.get("url"));
                    resource.setThumbnail(uploadResult.get("thumbnail"));
                    resource.setWatermark(uploadResult.get("watermark"));
                    resource.setHosting("cloudinary");
                    
                    // Usar el tipo detectado automáticamente
                    resource.setType(detectedType);
                    
                    // Solo actualizamos isBanner si se proporciona un valor
                    if (uploadDTO.getIsBanner() != null) {
                        resource.setIsBanner(uploadDTO.getIsBanner());
                    }
                    
                    resource.setPublicId(uploadResult.get("publicId"));
                    
                    return resourceRepository.save(resource);
                }
            } catch (Exception e) {
                log.error("Error updating resource with file", e);
            }
            return null;
        } else if (resource != null && (file == null || file.isEmpty())) {
            // Si no se proporciona un archivo, simplemente actualizamos los campos de tipo nombre y banner
            if (uploadDTO.getName() != null) {
                resource.setName(uploadDTO.getName());
            }
            if (uploadDTO.getIsBanner() != null) {
                resource.setIsBanner(uploadDTO.getIsBanner());
            }
            resourceRepository.save(resource);
        }
        return resource;
    }
    
    @Override
    public Boolean deleteResource(String id) {
        try {
            Integer resourceId = Integer.parseInt(id);
            Resource resource = resourceRepository.getById(resourceId);
            if (resource != null) {
                cloudinaryService.deleteImage(resource.getPublicId());
                resourceRepository.delete(resource);
                return true;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid resource ID format: {}", id, e);
        }
        return false;
    }

    @Override
    public List<Category> getResourceCategories(String resourceId) {
        Resource resource = getResourceById(resourceId);
        if (resource != null) {
            return resource.getCategories();
        }
        return null;
    }

    @Override
    public Resource addCategoriesToResource(String resourceId, List<String> categoryIds) {
        try {
            Resource resource = getResourceById(resourceId);
            if (resource != null && categoryIds != null && !categoryIds.isEmpty()) {
                List<Category> existingCategories = resource.getCategories();
                for (String categoryId : categoryIds) {
                    Category category = getCategoryById(Integer.parseInt(categoryId));
                    if (category != null && !existingCategories.contains(category)) {
                        existingCategories.add(category);
                    }
                }
                resource.setCategories(existingCategories);
                return resourceRepository.save(resource);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid category ID format in the list", e);
        }
        return null;
    }

    @Override
    public Resource replaceResourceCategories(String resourceId, List<String> categoryIds) {
        try {
            Resource resource = getResourceById(resourceId);
            if (resource != null && categoryIds != null) {
                List<Category> newCategories = new ArrayList<>();
                for (String categoryId : categoryIds) {
                    Category category = getCategoryById(Integer.parseInt(categoryId));
                    if (category != null) {
                        newCategories.add(category);
                    }
                }
                resource.setCategories(newCategories);
                return resourceRepository.save(resource);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid category ID format in the list", e);
        }
        return null;
    }

    @Override
    public List<Attribute> getResourceAttributes(String resourceId) {
        Resource resource = getResourceById(resourceId);
        if (resource != null) {
            return resource.getAttributes();
        }
        return null;
    }

    @Override
    public Resource addAttributesToResource(String resourceId, List<String> attributeIds) {
        try {
            Resource resource = getResourceById(resourceId);
            if (resource != null && attributeIds != null && !attributeIds.isEmpty()) {
                List<Attribute> existingAttributes = resource.getAttributes();
                for (String attributeId : attributeIds) {
                    Attribute attribute = getAttributeById(Long.parseLong(attributeId));
                    if (attribute != null && !existingAttributes.contains(attribute)) {
                        existingAttributes.add(attribute);
                    }
                }
                resource.setAttributes(existingAttributes);
                return resourceRepository.save(resource);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid attribute ID format in the list", e);
        }
        return null;
    }

    @Override
    public Resource replaceResourceAttributes(String resourceId, List<String> attributeIds) {
        try {
            Resource resource = getResourceById(resourceId);
            if (resource != null && attributeIds != null) {
                List<Attribute> newAttributes = new ArrayList<>();
                for (String attributeId : attributeIds) {
                    Attribute attribute = getAttributeById(Long.parseLong(attributeId));
                    if (attribute != null) {
                        newAttributes.add(attribute);
                    }
                }
                resource.setAttributes(newAttributes);
                return resourceRepository.save(resource);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid attribute ID format in the list", e);
        }
        return null;
    }

    private Category getCategoryById(Integer id) {
        return categoryRepository.getById(id);
    }

    private Attribute getAttributeById(Long id) {
        return attributeRepository.getById(id);
    }
    
    /**
     * Determina automáticamente el tipo de recurso basado en el contenido del archivo
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
            } else if (lowerContentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || 
                       lowerContentType.equals("application/msword")) {
                return ResourceType.DOCX;
            }
        }
        
        // Si no se pudo detectar por contentType, intentamos por la extensión del nombre
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
}
