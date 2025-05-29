package com.playmatsec.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.ShippingAddressDTO;
import com.playmatsec.app.repository.ShippingAddressRepository;
import com.playmatsec.app.repository.model.ShippingAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingAddressServiceImpl implements ShippingAddressService {
    private final ShippingAddressRepository shippingAddressRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<ShippingAddress> getShippingAddresses(String user, String fullname, String phone, String country, String state) {
        if (StringUtils.hasLength(user) || StringUtils.hasLength(fullname) || StringUtils.hasLength(phone) || StringUtils.hasLength(country) || StringUtils.hasLength(state)) {
            return shippingAddressRepository.search(user, fullname, phone, country, state);
        }
        List<ShippingAddress> addresses = shippingAddressRepository.getShippingAddresses();
        return addresses.isEmpty() ? null : addresses;
    }

    @Override
    public ShippingAddress getShippingAddressById(String id) {
        try {
            Integer shippingAddressId = Integer.parseInt(id);
            return shippingAddressRepository.getById(shippingAddressId);
        } catch (NumberFormatException e) {
            log.error("Invalid shipping address ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public ShippingAddress createShippingAddress(ShippingAddressDTO request) {
        if (request != null && StringUtils.hasLength(request.getAddressLine1())) {
            ShippingAddress shippingAddress = objectMapper.convertValue(request, ShippingAddress.class);
            return shippingAddressRepository.save(shippingAddress);
        }
        return null;
    }

    @Override
    public ShippingAddress updateShippingAddress(String id, String request) {
        ShippingAddress shippingAddress = getShippingAddressById(id);
        if (shippingAddress != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(shippingAddress)));
                ShippingAddress patched = objectMapper.treeToValue(target, ShippingAddress.class);
                shippingAddressRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating shipping address {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public ShippingAddress updateShippingAddress(String id, ShippingAddressDTO request) {
        ShippingAddress shippingAddress = getShippingAddressById(id);
        if (shippingAddress != null) {
            // shippingAddress.update(request); // Implementar si existe método update
            shippingAddressRepository.save(shippingAddress);
            return shippingAddress;
        }
        return null;
    }

    @Override
    public Boolean deleteShippingAddress(String id) {
        try {
            Integer shippingAddressId = Integer.parseInt(id);
            ShippingAddress shippingAddress = shippingAddressRepository.getById(shippingAddressId);
            if (shippingAddress != null) {
                shippingAddressRepository.delete(shippingAddress);
                return true;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid shipping address ID format: {}", id, e);
        }
        return false;
    }
}
