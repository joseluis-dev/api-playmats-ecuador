package com.playmatsec.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.ResourceDTO;
import com.playmatsec.app.repository.ResourceRepository;
import com.playmatsec.app.repository.model.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Resource> getResources(String name, String url, String hosting) {
        if (StringUtils.hasLength(name) || StringUtils.hasLength(url) || StringUtils.hasLength(hosting)) {
            return resourceRepository.search(name, url, hosting);
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
    public Resource createResource(ResourceDTO request) {
        if (request != null && StringUtils.hasLength(request.getName())) {
            Resource resource = objectMapper.convertValue(request, Resource.class);
            return resourceRepository.save(resource);
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
                resourceRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating resource {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Resource updateResource(String id, ResourceDTO request) {
        Resource resource = getResourceById(id);
        if (resource != null) {
            // resource.update(request); // Implementar si existe método update
            resourceRepository.save(resource);
            return resource;
        }
        return null;
    }

    @Override
    public Boolean deleteResource(String id) {
        try {
            Integer resourceId = Integer.parseInt(id);
            Resource resource = resourceRepository.getById(resourceId);
            if (resource != null) {
                resourceRepository.delete(resource);
                return true;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid resource ID format: {}", id, e);
        }
        return false;
    }
}
