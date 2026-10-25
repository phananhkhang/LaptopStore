package org.akira.laptopstore.procurement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupplierRequest {
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;

    private String phone;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String address;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}
