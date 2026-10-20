package org.akira.laptopstore.user.service;

import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.user.dto.AddressResponse;
import org.akira.laptopstore.user.dto.CreateAddressRequest;
import org.akira.laptopstore.user.entity.Address;
import org.akira.laptopstore.user.repository.AddressRepository;
import org.akira.laptopstore.user.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address sampleAddress;

    @BeforeEach
    void setUp() {
        sampleAddress = Address.builder()
                .id(1L)
                .userId(10L)
                .recipientName("Nguyen Van A")
                .phone("0987654321")
                .street("123 Nguyen Trai")
                .ward("Phuong 2")
                .district("Quan 5")
                .city("TP Ho Chi Minh")
                .postalCode("700000")
                .isDefault(true)
                .build();
    }

    @Test
    @DisplayName("getUserAddresses: Should return addresses list for user")
    void getUserAddresses_shouldReturnList() {
        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(10L))
                .thenReturn(List.of(sampleAddress));

        List<AddressResponse> responses = addressService.getUserAddresses(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRecipientName()).isEqualTo("Nguyen Van A");
        assertThat(responses.get(0).getCity()).isEqualTo("TP Ho Chi Minh");
    }

    @Test
    @DisplayName("getAddressById: Should return address when found")
    void getAddressById_whenFound_shouldReturnAddress() {
        when(addressRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(sampleAddress));

        AddressResponse response = addressService.getAddressById(10L, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRecipientName()).isEqualTo("Nguyen Van A");
    }

    @Test
    @DisplayName("getAddressById: Should throw ResourceNotFoundException when address not found")
    void getAddressById_whenNotFound_shouldThrowException() {
        when(addressRepository.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddressById(10L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Address not found");
    }

    @Test
    @DisplayName("createAddress: Should save and return created address")
    void createAddress_shouldSaveAndReturn() {
        CreateAddressRequest request = new CreateAddressRequest();
        request.setRecipientName("Le Thi B");
        request.setPhone("0901234567");
        request.setStreet("456 Le Loi");
        request.setCity("Ha Noi");
        request.setIsDefault(false);

        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address a = invocation.getArgument(0);
            a.setId(2L);
            return a;
        });

        AddressResponse response = addressService.createAddress(10L, request);

        assertThat(response).isNotNull();
        assertThat(response.getRecipientName()).isEqualTo("Le Thi B");
        assertThat(response.getCity()).isEqualTo("Ha Noi");
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("deleteAddress: Should delete address when exists")
    void deleteAddress_whenExists_shouldDelete() {
        when(addressRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(sampleAddress));

        addressService.deleteAddress(10L, 1L);

        verify(addressRepository).delete(sampleAddress);
    }
}
