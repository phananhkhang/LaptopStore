package org.akira.laptopstore.catalog.mapper;

import org.akira.laptopstore.catalog.dto.response.ProductImageDto;
import org.akira.laptopstore.catalog.dto.response.ProductResponse;
import org.akira.laptopstore.catalog.dto.response.ProductSpecificationDto;
import org.akira.laptopstore.catalog.dto.response.VariantSnapshot;
import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.entity.ProductImage;
import org.akira.laptopstore.catalog.entity.ProductSpecification;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.catalog.dto.response.ProductSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "category.name", target = "categoryName")
    // Đặt Mapping thumbnailImage ở đúng hàm trả về ProductSummaryResponse
    @Mapping(target = "thumbnailImage", expression = "java(getThumbnail(product))")
    ProductSummaryResponse toProductSummaryResponse(Product product);

    // THÊM ĐOẠN NÀY VÀO ĐỂ FIX LỖI Ở CONTROLLER
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toResponse(Product product);

    ProductImageDto toProductImageDto(ProductImage image);
    ProductSpecificationDto toProductSpecificationDto(ProductSpecification spec);
    VariantSnapshot toVariantSnapshot(ProductVariant variant);

    // Default method trong interface cho phép em viết logic Java thuần
    // MapStruct sẽ tự động gọi hàm này khi xử lý biểu thức ở trên
    default String getThumbnail(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(product.getImages().get(0).getImageUrl()); // Fallback lấy ảnh đầu tiên nếu không có ảnh chính
    }
}