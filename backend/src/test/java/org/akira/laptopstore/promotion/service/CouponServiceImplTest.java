package org.akira.laptopstore.promotion.service;

import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.promotion.dto.request.CreateCouponRequest;
import org.akira.laptopstore.promotion.dto.request.UpdateCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponEvaluation;
import org.akira.laptopstore.promotion.entity.Coupon;
import org.akira.laptopstore.promotion.entity.CouponUsage;
import org.akira.laptopstore.promotion.enums.CouponUsageStatus;
import org.akira.laptopstore.promotion.enums.DiscountType;
import org.akira.laptopstore.promotion.exception.CouponCodeConflictException;
import org.akira.laptopstore.promotion.exception.CouponNotApplicableException;
import org.akira.laptopstore.promotion.repository.CouponRepository;
import org.akira.laptopstore.promotion.repository.CouponUsageRepository;
import org.akira.laptopstore.promotion.service.Impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {
    @Mock private CouponRepository coupons;
    @Mock private CouponUsageRepository usages;
    private CouponServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CouponServiceImpl(coupons, usages);
    }

    // ---------- CRUD ----------

    @Test
    void createRejectsDuplicateCodeIgnoringCaseAndWhitespace() {
        when(coupons.existsByCodeIgnoreCase("SALE10")).thenReturn(true);

        assertThrows(CouponCodeConflictException.class,
                () -> service.createCoupon(createRequest("  sale10 ")));
        verify(coupons, never()).save(any());
    }

    @Test
    void createNormalizesCodeToUpperCase() {
        when(coupons.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("SALE10", service.createCoupon(createRequest(" sale10 ")).getCode());
    }

    @Test
    void createRejectsPercentOver100() {
        CreateCouponRequest request = createRequest("SALE10");
        request.setDiscountValue(new BigDecimal("101"));

        assertThrows(ValidationException.class, () -> service.createCoupon(request));
    }

    @Test
    void getByIdThrowsNotFound() {
        when(coupons.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCouponById(99L));
    }

    @Test
    void updateKeepsActiveWhenNotSent() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        when(coupons.findById(1L)).thenReturn(Optional.of(coupon));
        when(coupons.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UpdateCouponRequest request = new UpdateCouponRequest();
        request.setCode("SALE10");
        request.setDiscountType(DiscountType.PERCENT);
        request.setDiscountValue(new BigDecimal("15"));

        assertTrue(service.updateCoupon(1L, request).isActive());
    }

    @Test
    void deleteUsedCouponOnlyDeactivates() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        when(coupons.findById(1L)).thenReturn(Optional.of(coupon));
        when(usages.existsByCouponId(1L)).thenReturn(true);

        service.deleteCoupon(1L);

        assertFalse(coupon.isActive());
        verify(coupons, never()).delete(any());
    }

    @Test
    void deleteUnusedCouponRemovesIt() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        when(coupons.findById(1L)).thenReturn(Optional.of(coupon));
        when(usages.existsByCouponId(1L)).thenReturn(false);

        service.deleteCoupon(1L);

        verify(coupons).delete(coupon);
    }

    // ---------- Điều kiện áp dụng ----------

    @Test
    void expiredCouponIsNotApplicable() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        coupon.setEndsAt(Instant.now().minus(Duration.ofMinutes(1)));
        when(coupons.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(coupon));

        assertThrows(CouponNotApplicableException.class,
                () -> service.evaluateForCheckout("sale10", 42L, new BigDecimal("1000000")));
    }

    @Test
    void notStartedCouponIsNotApplicable() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        coupon.setStartsAt(Instant.now().plus(Duration.ofDays(1)));
        when(coupons.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(coupon));

        assertThrows(CouponNotApplicableException.class,
                () -> service.evaluateForCheckout("SALE10", 42L, new BigDecimal("1000000")));
    }

    @Test
    void inactiveCouponIsNotApplicable() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        coupon.setActive(false);
        when(coupons.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(coupon));

        assertThrows(CouponNotApplicableException.class,
                () -> service.evaluateForCheckout("SALE10", 42L, new BigDecimal("1000000")));
    }

    @Test
    void subtotalBelowMinimumIsNotApplicable() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        coupon.setMinOrderAmount(new BigDecimal("5000000"));
        when(coupons.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(coupon));

        assertThrows(CouponNotApplicableException.class,
                () -> service.evaluateForCheckout("SALE10", 42L, new BigDecimal("1000000")));
    }

    @Test
    void totalUsageLimitReachedIsNotApplicable() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        coupon.setUsageLimit(5);
        when(coupons.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(coupon));
        when(usages.countByCouponIdAndStatus(1L, CouponUsageStatus.ACTIVE)).thenReturn(5L);

        assertThrows(CouponNotApplicableException.class,
                () -> service.evaluateForCheckout("SALE10", 42L, new BigDecimal("1000000")));
    }

    @Test
    void perUserLimitReachedIsNotApplicable() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        coupon.setUsagePerUser(1);
        when(coupons.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(coupon));
        when(usages.countByCouponIdAndUserIdAndStatus(1L, 42L, CouponUsageStatus.ACTIVE)).thenReturn(1L);

        assertThrows(CouponNotApplicableException.class,
                () -> service.evaluateForCheckout("SALE10", 42L, new BigDecimal("1000000")));
    }

    // ---------- Tính tiền giảm ----------

    @Test
    void percentDiscountIsCappedByMaxDiscount() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        coupon.setMaxDiscount(new BigDecimal("200000"));
        when(coupons.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(coupon));

        CouponEvaluation result = service.evaluateForCheckout("SALE10", 42L, new BigDecimal("5000000"));

        assertEquals(new BigDecimal("200000.00"), result.discountAmount());
    }

    @Test
    void fixedDiscountNeverExceedsSubtotal() {
        Coupon coupon = coupon(DiscountType.FIXED, "500000");
        when(coupons.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(coupon));

        CouponEvaluation result = service.evaluateForCheckout("SALE10", 42L, new BigDecimal("300000"));

        assertEquals(new BigDecimal("300000.00"), result.discountAmount());
    }

    // ---------- Reserve / release ----------

    @Test
    void reserveSavesActiveUsageWithDiscount() {
        Coupon coupon = coupon(DiscountType.PERCENT, "10");
        when(coupons.findByCodeForUpdate("SALE10")).thenReturn(Optional.of(coupon));

        service.reserveForOrder("sale10", 42L, 100L, new BigDecimal("1000000"));

        ArgumentCaptor<CouponUsage> captor = ArgumentCaptor.forClass(CouponUsage.class);
        verify(usages).save(captor.capture());
        assertEquals(100L, captor.getValue().getOrderId());
        assertEquals(CouponUsageStatus.ACTIVE, captor.getValue().getStatus());
        assertEquals(new BigDecimal("100000.00"), captor.getValue().getDiscountAmount());
    }

    @Test
    void reserveRejectsOrderThatAlreadyHasCoupon() {
        when(usages.existsByOrderId(100L)).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> service.reserveForOrder("SALE10", 42L, 100L, new BigDecimal("1000000")));
        verify(usages, never()).save(any());
    }

    @Test
    void releaseIsIdempotent() {
        CouponUsage usage = CouponUsage.builder()
                .id(5L).orderId(100L).status(CouponUsageStatus.ACTIVE).build();
        when(usages.findByOrderId(100L)).thenReturn(Optional.of(usage));

        service.releaseForOrder(100L);
        service.releaseForOrder(100L);

        assertEquals(CouponUsageStatus.RELEASED, usage.getStatus());
        verify(usages, times(1)).save(usage);
    }

    @Test
    void releaseWithoutUsageDoesNothing() {
        when(usages.findByOrderId(100L)).thenReturn(Optional.empty());

        service.releaseForOrder(100L);

        verify(usages, never()).save(any());
    }

    private CreateCouponRequest createRequest(String code) {
        CreateCouponRequest request = new CreateCouponRequest();
        request.setCode(code);
        request.setDiscountType(DiscountType.PERCENT);
        request.setDiscountValue(new BigDecimal("10"));
        return request;
    }

    private Coupon coupon(DiscountType type, String value) {
        return Coupon.builder()
                .id(1L)
                .code("SALE10")
                .discountType(type)
                .discountValue(new BigDecimal(value))
                .active(true)
                .build();
    }
}
