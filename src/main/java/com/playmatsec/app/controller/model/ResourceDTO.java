package com.playmatsec.app.controller.model;

import com.playmatsec.app.repository.utils.Consts.ResourceType;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResourceDTO {
    private String name;
    private String publicId;
    private String url;
    private String thumbnail;
    private String watermark;
    private String hosting;
    private ResourceType type;
}
