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
import com.playmatsec.app.controller.model.ResourceProductDTO;
import com.playmatsec.app.repository.ResourceRepository;
import com.playmatsec.app.repository.CategoryRepository;
import com.playmatsec.app.repository.AttributeRepository;
import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.repository.model.ResourceProduct;
import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.repository.model.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeRepository attributeRepository;
    private final CloudinaryService cloudinaryService;
    private final ResourceProductService resourceProductService;
    private final ObjectMapper objectMapper;

    @Override
    public List<Resource> getResources(String name, String url, String hosting, String thumbnail, String watermark, String type, Boolean isBanner, String product, String category) {
        if (StringUtils.hasLength(name) || StringUtils.hasLength(url) || StringUtils.hasLength(hosting) || StringUtils.hasLength(thumbnail) || StringUtils.hasLength(watermark) || StringUtils.hasLength(type) || isBanner != null || StringUtils.hasLength(product) || StringUtils.hasLength(category)) {
            return resourceRepository.search(name, url, hosting, thumbnail, watermark, type, isBanner, product, category);
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
                    resource.setPublicId(uploadResult.get("publicId"));

                    Resource savedResource = resourceRepository.save(resource);
                    
                    // Si se especificó un productId, crear la relación ResourceProduct
                    if (uploadDTO.getProductId() != null && !uploadDTO.getProductId().isEmpty()) {
                        try {
                            // Crear una nueva relación ResourceProduct
                            ResourceProductDTO resourceProductDTO = new ResourceProductDTO();
                            resourceProductDTO.setResourceId(savedResource.getId().toString());
                            resourceProductDTO.setProductId(uploadDTO.getProductId());
                            resourceProductDTO.setIsBanner(uploadDTO.getIsBanner());
                            
                            resourceProductService.createResourceProduct(resourceProductDTO);
                        } catch (Exception e) {
                            log.error("Error creating resource-product relationship", e);
                        }
                    }
                    
                    return savedResource;
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
                
                // Solo actualizamos los campos básicos del recurso
                // La relación con productos se maneja a través de ResourceProduct
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
                    
                    resource.setPublicId(uploadResult.get("publicId"));
                    
                    Resource savedResource = resourceRepository.save(resource);
                    
                    // Si se proporciona un productId y un valor de isBanner, actualizar o crear la relación ResourceProduct
                    if (uploadDTO.getProductId() != null && !uploadDTO.getProductId().isEmpty() && uploadDTO.getIsBanner() != null) {
                        ResourceProduct existingRelation = resourceProductService.getResourceProductByResourceIdAndProductId(
                            savedResource.getId().toString(), uploadDTO.getProductId());
                        
                        if (existingRelation != null) {
                            // Actualizar la relación existente
                            ResourceProductDTO updateDTO = new ResourceProductDTO();
                            updateDTO.setIsBanner(uploadDTO.getIsBanner());
                            resourceProductService.updateResourceProduct(existingRelation.getId().toString(), updateDTO);
                        } else {
                            // Crear una nueva relación
                            ResourceProductDTO newRelationDTO = new ResourceProductDTO();
                            newRelationDTO.setResourceId(savedResource.getId().toString());
                            newRelationDTO.setProductId(uploadDTO.getProductId());
                            newRelationDTO.setIsBanner(uploadDTO.getIsBanner());
                            resourceProductService.createResourceProduct(newRelationDTO);
                        }
                    }
                    
                    return savedResource;
                }
            } catch (Exception e) {
                log.error("Error updating resource with file", e);
            }
            return null;
        } else if (resource != null && (file == null || file.isEmpty())) {
            // Si no se proporciona un archivo, simplemente actualizamos el nombre
            if (uploadDTO.getName() != null) {
                resource.setName(uploadDTO.getName());
            }
            
            Resource savedResource = resourceRepository.save(resource);
            
            // Si se proporciona un productId y un valor de isBanner, actualizar o crear la relación ResourceProduct
            if (uploadDTO.getProductId() != null && !uploadDTO.getProductId().isEmpty() && uploadDTO.getIsBanner() != null) {
                ResourceProduct existingRelation = resourceProductService.getResourceProductByResourceIdAndProductId(
                    savedResource.getId().toString(), uploadDTO.getProductId());
                
                if (existingRelation != null) {
                    // Actualizar la relación existente
                    ResourceProductDTO updateDTO = new ResourceProductDTO();
                    updateDTO.setIsBanner(uploadDTO.getIsBanner());
                    resourceProductService.updateResourceProduct(existingRelation.getId().toString(), updateDTO);
                } else {
                    // Crear una nueva relación
                    ResourceProductDTO newRelationDTO = new ResourceProductDTO();
                    newRelationDTO.setResourceId(savedResource.getId().toString());
                    newRelationDTO.setProductId(uploadDTO.getProductId());
                    newRelationDTO.setIsBanner(uploadDTO.getIsBanner());
                    resourceProductService.createResourceProduct(newRelationDTO);
                }
            }
            
            return savedResource;
        }
        return resource;
    }
    
    @Override
    public Boolean deleteResource(String id) {
        try {
            Integer resourceId = Integer.parseInt(id);
            Resource resource = resourceRepository.getById(resourceId);
            if (resource != null) {
                String publicId = resource.getPublicId();
                resourceRepository.delete(resource);
                // Solo eliminar la imagen en Cloudinary si la eliminación en la base de datos fue exitosa
                if (publicId != null && !publicId.isEmpty()) {
                    cloudinaryService.deleteImage(publicId);
                }
                return true;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid resource ID format: {}", id, e);
        } catch (Exception e) {
            log.error("Error deleting resource with ID: {}", id, e);
            return false;
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
