package org.akira.laptopstore.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.akira.laptopstore.order.entity.enums.OrderStatus;

import java.time.Instant;

@Entity
@Table(name = "order_status_history")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "from_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus fromStatus;

    @Column(name = "to_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus toStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;
}
