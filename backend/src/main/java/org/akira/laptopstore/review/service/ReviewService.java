package org.akira.laptopstore.review.service;

import org.akira.laptopstore.review.dto.request.CreateReviewRequest;
import org.akira.laptopstore.review.dto.request.UpdateReviewRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.dto.response.ReviewSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /**
     * Tạo đánh giá cho một sản phẩm đã mua.
     *
     * User phải sở hữu order item,
     * đơn hàng phải DELIVERED
     * và mỗi order item chỉ được review một lần.
     */
    ReviewResponse create(
            Long userId,
            CreateReviewRequest request);

    /**
     * Cập nhật rating hoặc comment của review.
     *
     * Chỉ chủ sở hữu review được sửa.
     * Review đã bị staff ẩn thì không được chỉnh sửa.
     */
    ReviewResponse update(
            Long userId,
            Long reviewId,
            UpdateReviewRequest request);

    /**
     * Cho staff ẩn hoặc hiện một review.
     *
     * visible = true: review được hiển thị.
     * visible = false: review bị ẩn.
     */
    ReviewResponse setVisibility(
            Long reviewId,
            boolean isVisible);

    /**
     * Lấy chi tiết review công khai.
     * Chỉ trả về review đang visible.
     */
    ReviewResponse get(Long reviewId);

    /**
     * Lấy danh sách review đang hiển thị của một sản phẩm.
     * Có hỗ trợ phân trang.
     */
    Page<ReviewResponse> getProductReviews(
            Long productId,
            Pageable pageable);

    /**
     * Tính thống kê đánh giá của sản phẩm:
     * điểm trung bình, tổng số review
     * và số review theo từng mức 1-5 sao.
     */
    ReviewSummaryResponse getProductReviewSummary(
            Long productId);

    /**
     * Lấy lịch sử review của user.
     * Bao gồm cả review đang hiển thị và review đã bị staff ẩn.
     */
    Page<ReviewResponse> getMyReviews(
            Long userId,
            Pageable pageable);

    /**
     * Lấy danh sách review dành cho staff quản lý.
     *
     * Có thể lọc theo visible và rating.
     * Nếu tham số null thì không áp dụng điều kiện lọc tương ứng.
     */
    Page<ReviewResponse> getReviewsForStaff(
            Boolean visible,
            Integer rating,
            Pageable pageable);
}
