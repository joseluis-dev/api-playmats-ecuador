package com.playmatsec.app.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.playmatsec.app.config.Authorized;
import com.playmatsec.app.controller.model.ShippingAddressDTO;
import com.playmatsec.app.repository.model.ShippingAddress;
import com.playmatsec.app.service.ShippingAddressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@Authorized
public class ShippingController {
  private final ShippingAddressService shippingAddressService;

  @GetMapping("/shipping")
  public ResponseEntity<List<ShippingAddress>> getShippingAddresses(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String user,
    @RequestParam(required = false) String fullname,
    @RequestParam(required = false) String phone,
    @RequestParam(required = false) String country,
    @RequestParam(required = false) String state,
    @RequestParam(required = false) String city,
    @RequestParam(required = false) String postalCode,
    @RequestParam(required = false) String addressOne,
    @RequestParam(required = false) String addressTwo,
    @RequestParam(required = false) Boolean current,
    HttpServletRequest request
  ) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<ShippingAddress> addresses = shippingAddressService.getShippingAddresses(user, fullname, phone, country, state, city, postalCode, addressOne, addressTwo, current);
    return addresses != null ? ResponseEntity.ok(addresses) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/shipping/{id}")
  public ResponseEntity<ShippingAddress> getShippingAddressById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    ShippingAddress address = shippingAddressService.getShippingAddressById(id);
    return address != null ? ResponseEntity.ok(address) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/shipping/{id}")
  public ResponseEntity<Boolean> deleteShippingAddressById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    boolean deleted = shippingAddressService.deleteShippingAddress(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @PostMapping("/shipping")
  public ResponseEntity<ShippingAddress> createShippingAddress(@RequestHeader Map<String, String> headers, @RequestBody ShippingAddressDTO shippingAddress, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    ShippingAddress createdAddress = shippingAddressService.createShippingAddress(shippingAddress);
    return createdAddress != null ? ResponseEntity.ok(createdAddress) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/shipping/{id}")
  public ResponseEntity<ShippingAddress> updateShippingAddress(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    ShippingAddress updatedAddress = shippingAddressService.updateShippingAddress(id, patchBody);
    return updatedAddress != null ? ResponseEntity.ok(updatedAddress) : ResponseEntity.notFound().build();
  }

  @PutMapping("/shipping/{id}")
  public ResponseEntity<ShippingAddress> replaceShippingAddress(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody ShippingAddressDTO shippingAddress, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    ShippingAddress replacedAddress = shippingAddressService.updateShippingAddress(id, shippingAddress);
    return replacedAddress != null ? ResponseEntity.ok(replacedAddress) : ResponseEntity.notFound().build();
  }
}
