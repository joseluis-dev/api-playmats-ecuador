package com.playmatsec.app.controller.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResourceProductDTO {
    private String resourceId;
    private String productId;
    private Boolean isBanner;
}
