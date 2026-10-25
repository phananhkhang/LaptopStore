package org.akira.laptopstore.order.repository;

import org.akira.laptopstore.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("select oi.id from OrderItem oi where oi.productId = :productId")
    List<Long> findIdsByProductId(@Param("productId") Long productId);

    @Query("select oi.id from OrderItem oi where oi.order.userId = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    @Query("select oi from OrderItem oi join fetch oi.order where oi.id in :ids")
    List<OrderItem> findAllWithOrderByIdIn(@Param("ids") Collection<Long> ids);
}
