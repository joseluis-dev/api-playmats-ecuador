package com.playmatsec.app.controller.model;

import java.util.List;
import lombok.Data;

@Data
public class ReplaceProductResourcesDTO {
    private List<ResourceProductRequestDTO> resourcesProduct;
}