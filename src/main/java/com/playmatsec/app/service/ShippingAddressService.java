package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.ShippingAddress;
import com.playmatsec.app.controller.model.ShippingAddressDTO;

public interface ShippingAddressService {
    List<ShippingAddress> getShippingAddresses(String user, String fullname, String phone, String country, String state, String city, String postalCode, String addressOne, String addressTwo, Boolean current);
    ShippingAddress getShippingAddressById(String id);
    ShippingAddress createShippingAddress(ShippingAddressDTO shippingAddress);
    ShippingAddress updateShippingAddress(String id, String updateRequest);
    ShippingAddress updateShippingAddress(String id, ShippingAddressDTO shippingAddress);
    Boolean deleteShippingAddress(String id);
}
