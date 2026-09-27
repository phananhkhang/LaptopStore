package org.akira.laptopstore.user.service;

import org.akira.laptopstore.user.dto.AddressResponse;
import org.akira.laptopstore.user.dto.CreateAddressRequest;
import org.akira.laptopstore.user.dto.UpdateAddressRequest;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getUserAddresses(Long userId);
    AddressResponse getAddressById(Long userId, Long addressId);
    AddressResponse createAddress(Long userId, CreateAddressRequest request);
    AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request);
    void deleteAddress(Long userId, Long addressId);
}
