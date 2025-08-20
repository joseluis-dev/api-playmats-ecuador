package com.playmatsec.app.controller.model;

import com.playmatsec.app.repository.utils.Consts.ResourceType;
import lombok.Data;

@Data
public class ResourceUploadDTO {
    private ResourceType type;
    private Boolean isBanner;
    private String name;
}
