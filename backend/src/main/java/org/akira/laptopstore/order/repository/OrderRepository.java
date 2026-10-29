package org.akira.laptopstore.order.repository;

import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    @Query("select o from Order o where (:userId is null or o.userId = :userId) "
            + "and (:status is null or o.status = :status) "
            + "and (lower(o.orderCode) like concat('%', :search, '%') or lower(o.recipientName) like concat('%', :search, '%'))")
    Page<Order> search(@Param("userId") Long userId, @Param("status") OrderStatus status,
                       @Param("search") String search, Pageable pageable);

    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    Optional<Order> findByIdempotencyKeyAndUserId(String idempotencyKey, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.status = :status and o.createdAt < :cutoffTime order by o.id")
    List<Order> findByStatusAndCreatedAtBefore(@Param("status") OrderStatus status,
                                               @Param("cutoffTime") Instant cutoffTime);
}
