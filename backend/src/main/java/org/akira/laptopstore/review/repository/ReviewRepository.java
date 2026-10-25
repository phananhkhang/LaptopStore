package org.akira.laptopstore.review.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.akira.laptopstore.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrderItemId(Long orderItemId);

    boolean existsByOrderItemId(Long orderItemId);

    Optional<Review> findByIdAndVisibleTrue(Long id);

    // Review theo sản phẩm: orderItemIds lấy từ OrderQueryService.getOrderItemIdsByProduct
    Page<Review> findByOrderItemIdInAndVisibleTrue(Collection<Long> orderItemIds, Pageable pageable);

    // Review của tôi: orderItemIds lấy từ OrderQueryService.getOrderItemIdsByUser
    Page<Review> findByOrderItemIdIn(Collection<Long> orderItemIds, Pageable pageable);

    // Mỗi phần tử: [rating (Integer), số lượng (Long)]
    @Query("""
            select r.rating, count(r)
            from Review r
            where r.orderItemId in :orderItemIds and r.visible = true
            group by r.rating
            """)
    List<Object[]> countVisibleByRating(@Param("orderItemIds") Collection<Long> orderItemIds);

    // Danh sách cho staff duyệt
    Page<Review> findByVisible(Boolean visible, Pageable pageable);

    Page<Review> findByRating(Integer rating, Pageable pageable);

    Page<Review> findByVisibleAndRating(Boolean visible, Integer rating, Pageable pageable);
}
