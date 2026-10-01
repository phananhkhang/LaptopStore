package org.akira.laptopstore.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAddressRequest {
    @NotBlank(message = "Recipient name is required")
    @Size(max = 150)
    private String recipientName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20)
    private String phone;

    private String ward;
    private String district;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Street address is required")
    @Size(max = 255)
    private String street;

    private String postalCode;
    private Boolean isDefault;
}
