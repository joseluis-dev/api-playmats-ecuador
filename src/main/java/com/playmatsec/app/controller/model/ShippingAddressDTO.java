package com.playmatsec.app.controller.model;

import lombok.*;

import com.playmatsec.app.repository.model.Country;
import com.playmatsec.app.repository.model.State;
import com.playmatsec.app.repository.model.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ShippingAddressDTO {
    private User user;
    private String fullname;
    private String phone;
    private Country country;
    private State state;
    private String city;
    private String postalCode;
    private String addressOne;
    private String addressTwo;
    private Boolean current;
}
