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
import com.playmatsec.app.repository.CountryRepository;
import com.playmatsec.app.repository.ShippingAddressRepository;
import com.playmatsec.app.repository.StateRepository;
import com.playmatsec.app.repository.UserRepository;
import com.playmatsec.app.repository.model.Country;
import com.playmatsec.app.repository.model.ShippingAddress;
import com.playmatsec.app.repository.model.State;
import com.playmatsec.app.repository.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingAddressServiceImpl implements ShippingAddressService {
    private final ShippingAddressRepository shippingAddressRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<ShippingAddress> getShippingAddresses(String user, String fullname, String phone, String country, String state, String city, String postalCode, String addressOne, String addressTwo, Boolean current) {
        if (StringUtils.hasLength(user) || StringUtils.hasLength(fullname) || StringUtils.hasLength(phone) || StringUtils.hasLength(country) || StringUtils.hasLength(state) || StringUtils.hasLength(city) || StringUtils.hasLength(postalCode) || StringUtils.hasLength(addressOne) || StringUtils.hasLength(addressTwo) || current != null) {
            return shippingAddressRepository.search(user, fullname, phone, country, state, city, postalCode, addressOne, addressTwo, current);
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
        if (request != null
            && request.getUser() != null
            && StringUtils.hasLength(request.getFullname())
            && StringUtils.hasLength(request.getPhone())
            && request.getCountry() != null
            && request.getState() != null
            && StringUtils.hasLength(request.getCity())
            && StringUtils.hasLength(request.getPostalCode())
            && StringUtils.hasLength(request.getAddressOne())
            && StringUtils.hasLength(request.getAddressTwo())
            && request.getCurrent() != null
        ) {
            ShippingAddress shippingAddress = objectMapper.convertValue(request, ShippingAddress.class);
            if (request.getUser().getId() != null) {
                User user = userRepository.getById(request.getUser().getId());
                shippingAddress.setUser(user);
            }
            if (request.getCountry().getId() != null) {
                shippingAddress.setCountry(request.getCountry());
            }
            if (request.getState().getId() != null) {
                shippingAddress.setState(request.getState());
            }
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
                if (patched.getCountry() != null && patched.getCountry().getId() != null) {
                    Country country = countryRepository.getById(patched.getCountry().getId());
                    patched.setCountry(country);
                }
                if (patched.getState() != null && patched.getState().getId() != null) {
                    State state = stateRepository.getById(patched.getState().getId());
                    patched.setState(state);
                }
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
            if (request.getCountry() != null && request.getCountry().getId() != null) {
                Country country = countryRepository.getById(request.getCountry().getId());
                shippingAddress.setCountry(country);
            }
            if (request.getState() != null && request.getState().getId() != null) {
                State state = stateRepository.getById(request.getState().getId());
                shippingAddress.setState(state);
            }
            shippingAddress.update(request);
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
