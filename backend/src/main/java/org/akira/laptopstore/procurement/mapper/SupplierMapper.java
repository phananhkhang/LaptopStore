package org.akira.laptopstore.procurement.mapper;

import org.akira.laptopstore.procurement.dto.response.SupplierResponse;
import org.akira.laptopstore.procurement.entity.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierResponse toResponse(Supplier supplier);
}
