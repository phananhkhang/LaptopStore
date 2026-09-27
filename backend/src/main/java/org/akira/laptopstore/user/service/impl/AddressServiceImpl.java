package org.akira.laptopstore.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.user.dto.AddressResponse;
import org.akira.laptopstore.user.dto.CreateAddressRequest;
import org.akira.laptopstore.user.dto.UpdateAddressRequest;
import org.akira.laptopstore.user.entity.Address;
import org.akira.laptopstore.user.repository.AddressRepository;
import org.akira.laptopstore.user.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found or does not belong to user"));
        return toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse createAddress(Long userId, CreateAddressRequest request) {
        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault());

        if (isDefault) {
            resetDefaultAddress(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .recipientName(request.getRecipientName().trim())
                .phone(request.getPhone().trim())
                .ward(request.getWard() != null ? request.getWard().trim() : null)
                .district(request.getDistrict() != null ? request.getDistrict().trim() : null)
                .city(request.getCity().trim())
                .street(request.getStreet().trim())
                .postalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null)
                .isDefault(isDefault)
                .build();

        Address saved = addressRepository.save(address);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found or does not belong to user"));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            resetDefaultAddress(userId);
            address.setIsDefault(true);
        } else if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        address.setRecipientName(request.getRecipientName().trim());
        address.setPhone(request.getPhone().trim());
        address.setWard(request.getWard() != null ? request.getWard().trim() : null);
        address.setDistrict(request.getDistrict() != null ? request.getDistrict().trim() : null);
        address.setCity(request.getCity().trim());
        address.setStreet(request.getStreet().trim());
        address.setPostalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null);

        Address updated = addressRepository.save(address);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found or does not belong to user"));
        addressRepository.delete(address);
    }

    private void resetDefaultAddress(Long userId) {
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        for (Address addr : addresses) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUserId())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .street(address.getStreet())
                .postalCode(address.getPostalCode())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
