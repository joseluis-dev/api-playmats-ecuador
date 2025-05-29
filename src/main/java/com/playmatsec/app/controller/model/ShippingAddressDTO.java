package com.playmatsec.app.controller.model;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ShippingAddressDTO {
    private UUID id;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private StateDTO state;
    private CountryDTO country;
    private UserDTO user;
}
