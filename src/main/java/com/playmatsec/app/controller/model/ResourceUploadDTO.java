package com.playmatsec.app.controller.model;

import com.playmatsec.app.repository.utils.Consts.ResourceType;
import lombok.Data;

@Data
public class ResourceUploadDTO {
    private ResourceType type;
    private String name;
    private Boolean isBanner; // Mantenemos por compatibilidad, pero se usará en ResourceProduct
    private String productId; // ID del producto al que se asociará el recurso
}
