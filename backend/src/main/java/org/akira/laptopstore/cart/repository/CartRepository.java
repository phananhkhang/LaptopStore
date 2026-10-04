package org.akira.laptopstore.cart.repository;

import org.akira.laptopstore.cart.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Modifying
    @Query(
            value = """
                    insert into carts (user_id, created_at, updated_at)
                    values (:userId, current_timestamp, current_timestamp)
                    on conflict (user_id) do nothing
                    """,
            nativeQuery = true
    )
    int createCartIfAbsent(@Param("userId") Long userId);

    @EntityGraph(attributePaths = "items")
    @Query("select c from Cart c where c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.userId = :userId")
    Optional<Cart> findByUserIdForUpdate(@Param("userId") Long userId);
}
