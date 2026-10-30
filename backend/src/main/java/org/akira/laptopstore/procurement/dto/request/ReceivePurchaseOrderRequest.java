package org.akira.laptopstore.procurement.dto.request;

import org.akira.laptopstore.inventory.dto.request.PurchaseStockReceipt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivePurchaseOrderRequest {
    @NotEmpty(message = "Danh sách mặt hàng nhận không được để trống")
    @Valid
    private List<PurchaseStockReceipt> lines;

/*
* Kiệt đang kẹt ở một bài toán đánh đổi:
    - Nếu chỉ dùng poId + variantId → Sẽ bị lỗi nếu mai mốt user muốn nhận hàng đợt 2, đợt 3 cho cùng một phiếu nhập (vì key đã tồn tại từ đợt 1).
    - Nếu dùng thời gian → Chết tính Idempotency khi double-click.
* Giải pháp chuẩn Industry (Chuẩn Enterprise): "Client-Generated Idempotency Key"
    - Trách nhiệm sinh khóa Idempotency không được nằm ở Backend
    (vì Backend không biết đâu là request mới, đâu là request user bấm nhầm 2 lần).
    - Frontend phải là người sinh ra khóa này.
        + Khi Frontend mở popup "Xác nhận nhận hàng", nó tạo ra 1 mã UUID (Ví dụ: req-abc-123).
        + Nếu user bấm "Lưu" 3 lần liên tục, Frontend vẫn gửi đúng cái UUID req-abc-123 đó xuống Backend.
        + Backend dùng UUID này làm operationKey.
            → Request đầu tiên sẽ thành công, 2 request lặp lại sẽ bị block.
            → Lần nhận hàng đợt 2 vào ngày mai, Frontend sẽ sinh ra một UUID mới.
            → Hoàn hảo!
*/

    // Thêm UUID do Frontend gửi xuống để chống double-click
    @NotBlank(message = "Idempotency Key không được để trống")
    private String idempotencyKey;
}
