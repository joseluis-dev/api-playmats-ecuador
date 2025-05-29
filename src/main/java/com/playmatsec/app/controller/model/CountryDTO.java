package com.playmatsec.app.controller.model;

import lombok.*;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CountryDTO {
    private UUID id;
    private String name;
}
