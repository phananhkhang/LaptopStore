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
import org.mapstruct.MappingTarget; // Thêm import
import org.mapstruct.AfterMapping; // Thêm import

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "thumbnailImage", expression = "java(this.getThumbnail(product))")
    @Mapping(target = "startingPrice", expression = "java(this.getStartingPrice(product))")
    ProductSummaryResponse toProductSummaryResponse(Product product);

    // THÊM ĐOẠN NÀY VÀO ĐỂ FIX LỖI Ở CONTROLLER
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toProductResponseList(List<Product> products);

    ProductImageDto toProductImageDto(ProductImage image);
    List<ProductImageDto> toProductImageDtoList(List<ProductImage> images);
    ProductSpecificationDto toProductSpecificationDto(ProductSpecification spec);
    VariantSnapshot toVariantSnapshot(ProductVariant variant);

    default String getThumbnail(Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(java.util.Objects::nonNull)
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .map(image -> image.getImageUrl())
                .findFirst()
                .orElseGet(() -> product.getImages().stream()
                        .filter(java.util.Objects::nonNull)
                        .map(image -> image.getImageUrl())
                        .findFirst()
                        .orElse(null));
    }

    default BigDecimal getStartingPrice(Product product) {
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            return null;
        }

        return product.getVariants().stream()
                .filter(java.util.Objects::nonNull)
                .map(variant -> variant.getPrice()) // Entity ProductVariant bắt buộc phải có getPrice()
                .filter(java.util.Objects::nonNull)
                .min(java.util.Comparator.naturalOrder())
                .orElse(null);
    }

    @AfterMapping
    default void customizeProductSummaryResponse(Product product, @MappingTarget ProductSummaryResponse response) {
        // 1. Lấy ảnh thumbnail (ưu tiên ảnh có isPrimary = true)
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ProductImage img : product.getImages()) {
                if (Boolean.TRUE.equals(img.getIsPrimary())) {
                    response.setThumbnailImage(img.getImageUrl());
                    break;
                }
            }
            // Nếu không có ảnh nào được set primary, lấy tạm ảnh đầu tiên
            if (response.getThumbnailImage() == null) {
                response.setThumbnailImage(product.getImages().get(0).getImageUrl());
            }
        }

        // 2. Lấy giá nhỏ nhất làm startingPrice
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            BigDecimal minPrice = product.getVariants().stream()
                    .map(ProductVariant::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(null);
            response.setStartingPrice(minPrice);
        }
    }
}