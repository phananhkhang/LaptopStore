package org.akira.laptopstore.catalog.repository;

import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);

    // Nhờ có @SQLRestriction("active = true") ở Entity, hàm này sẽ tự động KHÔNG lấy các Variant đã bị Deactivate!
    List<ProductVariant> findByProductId(Long productId);

    boolean existsBySku(String sku);

    //Kiểm tra SKU trùng, nhưng bỏ qua Variant đang được cập nhật
    //Kiểm tra SKU đã tồn tại ở bản ghi khác (khác id hiện tại)
    boolean existsBySkuAndIdNot(String sku, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select variant from ProductVariant variant join fetch variant.product where variant.id in :variantIds order by variant.id")
    List<ProductVariant> findAllByIdInForUpdate(@Param("variantIds") List<Long> variantIds);

    //Dùng Native Query để chọc thẳng xuống DB, bỏ qua @SQLRestriction
    @Modifying
    @Query(value = "UPDATE product_variants SET active = :isActive WHERE id = :variantId AND product_id = :productId", nativeQuery = true)
    int updateActiveStatus(@Param("productId") Long productId, @Param("variantId") Long variantId, @Param("isActive") boolean isActive);

    // --- BỔ SUNG CHO INVENTORY QUERY CONTRACT ---

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.id = :variantId")
    Optional<ProductVariant> findByIdWithProduct(@Param("variantId") Long variantId);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.id IN :variantIds ORDER BY v.id ASC")
    List<ProductVariant> findAllByIdInWithProduct(@Param("variantIds") Collection<Long> variantIds);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product ORDER BY v.quantity ASC, v.id ASC")
    List<ProductVariant> findAllWithProductOrderByQuantityAsc();

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.quantity <= :threshold ORDER BY v.quantity ASC, v.id ASC")
    List<ProductVariant> findLowStockVariants(@Param("threshold") int threshold);
}
