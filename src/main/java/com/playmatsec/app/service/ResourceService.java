package com.playmatsec.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.repository.model.Attribute;
// import com.playmatsec.app.controller.model.ResourceDTO;
import com.playmatsec.app.controller.model.ResourceUploadDTO;

public interface ResourceService {
    List<Resource> getResources(String name, String url, String hosting, String thumbnail, String watermark, String type, Boolean isBanner, String product, String category);
    Resource getResourceById(String id);
    Resource createResource(MultipartFile file, ResourceUploadDTO uploadDTO);
    Resource updateResource(String id, String updateRequest);
    // Resource updateResource(String id, ResourceDTO resource);
    Resource updateResourceWithFile(String id, MultipartFile file, ResourceUploadDTO uploadDTO);
    Boolean deleteResource(String id);

    // Category management methods
    List<Category> getResourceCategories(String resourceId);
    Resource addCategoriesToResource(String resourceId, List<String> categoryIds);
    Resource replaceResourceCategories(String resourceId, List<String> categoryIds);

    // Attribute management methods
    List<Attribute> getResourceAttributes(String resourceId);
    Resource addAttributesToResource(String resourceId, List<String> attributeIds);
    Resource replaceResourceAttributes(String resourceId, List<String> attributeIds);
}
