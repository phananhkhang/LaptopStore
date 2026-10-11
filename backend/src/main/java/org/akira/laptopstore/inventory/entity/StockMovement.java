package org.akira.laptopstore.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(nullable = false)
    private String type; // Ví dụ: IMPORT, EXPORT, ADJUSTMENT

    @Column(nullable = false)
    private Integer delta; // Có thể âm hoặc dương

    @Column(nullable = false)
    private Integer quantityBefore;

    @Column(nullable = false)
    private Integer quantityAfter;

    @Column(nullable = false)
    private String referenceType; // Ví dụ: PURCHASE_ORDER, SALES_ORDER

    @Column(nullable = false)
    private String referenceId; // ID tham chiếu tới mã đơn nhập/đơn xuất

    @Column(nullable = false, unique = true)
    private String operationKey; // Khóa chống trùng lặp xử lý

    private String note;

    @Column(name = "created_by_user_id")
    private Long createdByUserId; // Lưu ID tạm để tránh dependency cứng vào User entity chưa hoàn thiện

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
