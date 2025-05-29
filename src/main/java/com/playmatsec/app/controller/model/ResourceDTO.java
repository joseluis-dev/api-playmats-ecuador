package com.playmatsec.app.controller.model;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResourceDTO {
    private UUID id;
    private String name;
    private String url;
    private String type;
}
