package org.akira.laptopstore.review.repository;

import java.util.Optional;

import org.akira.laptopstore.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    Optional<Review> findByOrderItemId(Long orderItemId);

    boolean existsByOrderItemId(Long orderItemId);

}
