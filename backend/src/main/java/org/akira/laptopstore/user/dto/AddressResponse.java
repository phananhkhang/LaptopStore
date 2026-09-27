package org.akira.laptopstore.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AddressResponse {
    private Long id;
    private Long userId;
    private String recipientName;
    private String phone;
    private String ward;
    private String district;
    private String city;
    private String street;
    private String postalCode;
    private Boolean isDefault;
    private Instant createdAt;
}
