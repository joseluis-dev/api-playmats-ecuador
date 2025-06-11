package com.playmatsec.app.controller.model;

import lombok.*;

import com.playmatsec.app.repository.model.Country;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class StateDTO {
    private String nombre;
    private Country country;
}
