package org.akira.laptopstore.procurement.mapper;

import org.akira.laptopstore.procurement.dto.response.PurchaseOrderItemResponse;
import org.akira.laptopstore.procurement.dto.response.PurchaseOrderResponse;

import org.akira.laptopstore.procurement.entity.PurchaseOrder;
import org.akira.laptopstore.procurement.entity.PurchaseOrderItem;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SupplierMapper.class})
public interface PurchaseOrderMapper {
    PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder);
    PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item);
}
