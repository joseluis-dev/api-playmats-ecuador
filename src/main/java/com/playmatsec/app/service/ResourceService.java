package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.controller.model.ResourceDTO;

public interface ResourceService {
    List<Resource> getResources(String name, String url, String hosting, String thumbnail, String watermark, String type, Boolean isBanner);
    Resource getResourceById(String id);
    Resource createResource(ResourceDTO resource);
    Resource updateResource(String id, String updateRequest);
    Resource updateResource(String id, ResourceDTO resource);
    Boolean deleteResource(String id);
}
