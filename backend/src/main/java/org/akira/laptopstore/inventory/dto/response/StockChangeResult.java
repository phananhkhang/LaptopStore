package org.akira.laptopstore.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
    Không xóa file này vì dùng cho kết quả trả về khi điều chỉnh kho

    hoặc nhận hàng nhập từ procurement sang inventory

    đúng như bảng định hướng trong inventory/README.md

    Vì trong inventory/README.md có liệt kê StockChangeResult là Response/contract của module inventory,

    và khi ADMIN gọi API POST /api/v1/inventory/variants/{variantId}/adjust để điều chỉnh tồn kho,

    thay vì chỉ trả một chuỗi text "Điều chỉnh tồn kho thành công!" khô khan như code cũ,

    ta trả về đối tượng StockChangeResult (hoặc StockMovementResponse) chứa rõ

        quantityBefore, delta, quantityAfter thì API sẽ chuyên nghiệp hơn gấp 10 lần!

    Kết quả trả về sau khi thực hiện thay đổi tồn kho tại module inventory.
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockChangeResult {
    private Long movementId, variantID;
    private int quantityBefore, quantityAfter;
    private String sku, operationKey;
    private Integer delta;
}
