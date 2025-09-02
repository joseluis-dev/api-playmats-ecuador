package com.playmatsec.app.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
        addresses.sort((a, b) -> a.getId().compareTo(b.getId()));
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
            if (request.getCurrent() != null && request.getCurrent()) {
                List<ShippingAddress> userAddresses = getShippingAddresses(
                    shippingAddress.getUser().getId() != null ? shippingAddress.getUser().getId().toString() : null,
                    null, null, null, null, null, null, null, null, null
                );
                for (ShippingAddress addr : userAddresses) {
                    if (!addr.getId().equals(shippingAddress.getId()) && Boolean.TRUE.equals(addr.getCurrent())) {
                        addr.setCurrent(false);
                        shippingAddressRepository.save(addr);
                    }
                }
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
                JsonNode requestNode = objectMapper.readTree(request);
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(requestNode);

                JsonNode currentNode = objectMapper.valueToTree(shippingAddress);
                JsonNode patchedNode = jsonMergePatch.apply(currentNode);

                objectMapper.readerForUpdating(shippingAddress).readValue(patchedNode.traverse());

                // Resolver relaciones SOLO si vinieron en el payload
                if (requestNode.has("user")) {
                    JsonNode userNode = requestNode.get("user");
                    if (userNode != null && !userNode.isNull()) {
                        if (userNode.hasNonNull("id")) {
                            UUID userId = UUID.fromString(userNode.get("id").asText()); // <-- evitar convertValue a User
                            User user = userRepository.getById(userId);
                            shippingAddress.setUser(user);
                        } else if (userNode.isTextual()) {
                            // Soportar también "user": "uuid"
                            UUID userId = UUID.fromString(userNode.asText());
                            User user = userRepository.getById(userId);
                            shippingAddress.setUser(user);
                        } else {
                            shippingAddress.setUser(null);
                        }
                    }
                }
                if (requestNode.has("country")) {
                    JsonNode countryNode = requestNode.get("country");
                    if (countryNode != null && !countryNode.isNull() && countryNode.hasNonNull("id")) {
                        Country country = countryRepository.getById(countryNode.get("id").asInt());
                        shippingAddress.setCountry(country);
                    } else if (countryNode == null || countryNode.isNull()) {
                        shippingAddress.setCountry(null);
                    }
                }
                if (requestNode.has("state")) {
                    JsonNode stateNode = requestNode.get("state");
                    if (stateNode != null && !stateNode.isNull() && stateNode.hasNonNull("id")) {
                        State state = stateRepository.getById(stateNode.get("id").asInt());
                        shippingAddress.setState(state);
                    } else if (stateNode == null || stateNode.isNull()) {
                        shippingAddress.setState(null);
                    }
                }
                if (requestNode.has("current") && requestNode.get("current").asBoolean(true)) {
                    List<ShippingAddress> userAddresses = getShippingAddresses(
                        shippingAddress.getUser().getId() != null ? shippingAddress.getUser().getId().toString() : null,
                        null, null, null, null, null, null, null, null, null
                    );
                    for (ShippingAddress addr : userAddresses) {
                        if (!addr.getId().equals(shippingAddress.getId()) && Boolean.TRUE.equals(addr.getCurrent())) {
                            addr.setCurrent(false);
                            shippingAddressRepository.save(addr);
                        }
                    }
                }
                shippingAddressRepository.save(shippingAddress);
                return shippingAddress;
            } catch (JsonPatchException | IOException e) {
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
            if (request.getUser() != null && request.getUser().getId() != null) {
                User user = userRepository.getById(request.getUser().getId());
                shippingAddress.setUser(user);
            }
            if (request.getCountry() != null && request.getCountry().getId() != null) {
                Country country = countryRepository.getById(request.getCountry().getId());
                shippingAddress.setCountry(country);
            }
            if (request.getState() != null && request.getState().getId() != null) {
                State state = stateRepository.getById(request.getState().getId());
                shippingAddress.setState(state);
            }
            if (request.getCurrent() != null && request.getCurrent()) {
                List<ShippingAddress> userAddresses = getShippingAddresses(
                    shippingAddress.getUser().getId() != null ? shippingAddress.getUser().getId().toString() : null,
                    null, null, null, null, null, null, null, null, null
                );
                for (ShippingAddress addr : userAddresses) {
                    if (!addr.getId().equals(shippingAddress.getId()) && Boolean.TRUE.equals(addr.getCurrent())) {
                        addr.setCurrent(false);
                        shippingAddressRepository.save(addr);
                    }
                }
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
