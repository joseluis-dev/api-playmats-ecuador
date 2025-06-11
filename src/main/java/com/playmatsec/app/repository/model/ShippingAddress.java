package com.playmatsec.app.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.playmatsec.app.controller.model.ShippingAddressDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipping_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private String fullname;
    private String phone;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;

    private String city;
    private String postalCode;
    private String addressOne;
    private String addressTwo;
    private Boolean current;

    public void update(ShippingAddressDTO shippingAddress) {
        this.fullname = shippingAddress.getFullname();
        this.phone = shippingAddress.getPhone();
        this.country = shippingAddress.getCountry();
        this.state = shippingAddress.getState();
        this.city = shippingAddress.getCity();
        this.postalCode = shippingAddress.getPostalCode();
        this.addressOne = shippingAddress.getAddressOne();
        this.addressTwo = shippingAddress.getAddressTwo();
        this.current = shippingAddress.getCurrent();
    }
}
