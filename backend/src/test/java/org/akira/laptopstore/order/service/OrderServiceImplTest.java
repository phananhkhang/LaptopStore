package org.akira.laptopstore.order.service;

import org.akira.laptopstore.cart.dto.response.CartItemResponse;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.dto.response.CartSnapshot;
import org.akira.laptopstore.cart.service.CartService;
import org.akira.laptopstore.catalog.dto.response.CheckoutVariantSnapshot;
import org.akira.laptopstore.catalog.service.CatalogCheckoutService;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.inventory.service.InventoryService;
import org.akira.laptopstore.order.dto.request.CheckoutPreviewRequest;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.OrderItem;
import org.akira.laptopstore.order.entity.OrderStatusHistory;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.mapper.OrderMapper;
import org.akira.laptopstore.order.repository.OrderItemRepository;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.repository.OrderStatusHistoryRepository;
import org.akira.laptopstore.order.service.impl.OrderServiceImpl;
import org.akira.laptopstore.promotion.dto.response.CouponEvaluation;
import org.akira.laptopstore.promotion.service.CouponService;
import org.akira.laptopstore.user.dto.AddressResponse;
import org.akira.laptopstore.user.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orders;
    @Mock private OrderItemRepository orderItems;
    @Mock private OrderStatusHistoryRepository histories;
    @Mock private CartService cartService;
    @Mock private CatalogCheckoutService catalogCheckoutService;
    @Mock private InventoryService inventoryService;
    @Mock private AddressService addressService;
    @Mock private CouponService couponService;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                orders,
                orderItems,
                histories,
                new OrderMapper(),
                cartService,
                catalogCheckoutService,
                inventoryService,
                addressService,
                couponService
        );
    }

    @Test
    void ownerCanReadDetailWithStoredItemAndStatusSnapshots() {
        Order order = order(10L, 42L);
        when(orders.findById(10L)).thenReturn(Optional.of(order));
        when(orderItems.findByOrderId(10L)).thenReturn(List.of(OrderItem.builder()
                .id(20L).productId(2L).variantId(3L).productName("Laptop")
                .variantName("16GB / 512GB").sku("LP-3").unitPrice(new BigDecimal("12500000"))
                .quantity(2).build()));
        when(histories.findByOrderIdOrderByChangedAtDesc(10L)).thenReturn(List.of(OrderStatusHistory.builder()
                .id(30L).toStatus(OrderStatus.PENDING).changedAt(Instant.parse("2026-10-01T10:00:00Z"))
                .changedByUserId(42L).build()));

        var response = service.getOwnOrder(42L, 10L);

        assertEquals("ORD-10", response.getOrderCode());
        assertEquals(new BigDecimal("25000000"), response.getItems().getFirst().getLineTotal());
        assertEquals(OrderStatus.PENDING, response.getStatusHistories().getFirst().getToStatus());
    }

    @Test
    void userCannotReadAnotherUsersOrder() {
        when(orders.findById(10L)).thenReturn(Optional.of(order(10L, 99L)));

        assertThrows(ResourceNotFoundException.class, () -> service.getOwnOrder(42L, 10L));
    }

    @Test
    void orderListsContainTheQuantityAcrossAllLines() {
        Order order = order(10L, 42L);
        when(orders.findByUserId(42L, PageRequest.of(0, 20))).thenReturn(new PageImpl<>(List.of(order)));
        when(orderItems.findByOrderId(10L)).thenReturn(List.of(
                OrderItem.builder().quantity(2).build(), OrderItem.builder().quantity(1).build()));

        var result = service.getOwnOrders(42L, PageRequest.of(0, 20));

        assertEquals(3, result.getContent().getFirst().getTotalItems());
    }

    @Test
    void createOrderWithAddressIdAndCouponCalculatesTotalsAndValidatesStock() {
        when(addressService.getAddressById(42L, 15L)).thenReturn(AddressResponse.builder()
                .id(15L)
                .userId(42L)
                .recipientName("Phan Anh Khang")
                .phone("0901234567")
                .street("123 Nguyen Trai")
                .ward("Phuong 2")
                .district("Quan 5")
                .city("TP. Ho Chi Minh")
                .postalCode("700000")
                .isDefault(true)
                .build());

        when(cartService.lockAndGetCheckoutCart(42L)).thenReturn(new CartSnapshot(3L, 42L,
                List.of(new CartSnapshot.Item(9L, 7L, 2))));
        when(catalogCheckoutService.lockAndGetSellableVariants(List.of(7L))).thenReturn(List.of(
                new CheckoutVariantSnapshot(7L, 70L, "MacBook Air M3", "16GB - 512GB - Silver",
                        "MBA-M3-16-512", new BigDecimal("15000000.00"), 5)));

        CouponEvaluation evaluation = new CouponEvaluation(1L, "STUDENT10", new BigDecimal("1500000.00"));
        when(couponService.evaluateForCheckout("STUDENT10", 42L, new BigDecimal("30000000.00")))
                .thenReturn(evaluation);
        when(couponService.reserveForOrder("STUDENT10", 42L, 100L, new BigDecimal("30000000.00")))
                .thenReturn(evaluation);

        when(orders.saveAndFlush(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.setCreatedAt(Instant.parse("2026-10-14T10:00:00Z"));
            saved.setUpdatedAt(saved.getCreatedAt());
            return saved;
        });
        when(orderItems.saveAll(any())).thenAnswer(invocation -> {
            Iterable<OrderItem> input = invocation.getArgument(0);
            List<OrderItem> list = new ArrayList<>();
            input.forEach(list::add);
            return list;
        });
        when(histories.save(any(OrderStatusHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = CreateOrderRequest.builder()
                .addressId(15L)
                .paymentMethod("VNPAY")
                .couponCode("student10")
                .idempotencyKey("idem-week4-001")
                .build();

        var response = service.createOrder(42L, request);

        assertEquals(100L, response.getId());
        assertEquals("Phan Anh Khang", response.getRecipientName());
        assertEquals("0901234567", response.getRecipientPhone());
        assertEquals("123 Nguyen Trai, Phuong 2, Quan 5, TP. Ho Chi Minh, 700000", response.getShippingAddress());
        assertEquals(new BigDecimal("30000000.00"), response.getSubtotal());
        assertEquals(new BigDecimal("1500000.00"), response.getDiscountAmount());
        assertEquals(BigDecimal.ZERO, response.getShippingFee());
        assertEquals(new BigDecimal("28500000.00"), response.getTotalAmount());
        assertEquals("STUDENT10", response.getCouponCodeSnapshot());

        verify(couponService).evaluateForCheckout("STUDENT10", 42L, new BigDecimal("30000000.00"));
        verify(couponService).reserveForOrder("STUDENT10", 42L, 100L, new BigDecimal("30000000.00"));
        verify(inventoryService).deductForOrder(eq(100L), eq(42L), any());
        verify(cartService).clearPurchasedItems(42L, List.of(9L));
    }

    @Test
    void createOrderRejectsInsufficientStockBeforeSavingOrder() {
        when(cartService.lockAndGetCheckoutCart(42L)).thenReturn(new CartSnapshot(3L, 42L,
                List.of(new CartSnapshot.Item(9L, 7L, 4))));
        when(catalogCheckoutService.lockAndGetSellableVariants(List.of(7L))).thenReturn(List.of(
                new CheckoutVariantSnapshot(7L, 70L, "MacBook Air M3", "16GB - 512GB - Silver",
                        "MBA-M3-16-512", new BigDecimal("15000000.00"), 2)));

        CreateOrderRequest request = new CreateOrderRequest(
                "Nguyen Van A", "0900000000", "Ho Chi Minh City", null, null, null);

        assertThrows(ValidationException.class, () -> service.createOrder(42L, request));

        verify(orders, never()).saveAndFlush(any());
        verifyNoInteractions(orderItems, histories, inventoryService, couponService);
    }

    @Test
    void createOrderRejectsInvalidCouponBeforeSavingOrder() {
        when(cartService.lockAndGetCheckoutCart(42L)).thenReturn(new CartSnapshot(3L, 42L,
                List.of(new CartSnapshot.Item(9L, 7L, 1))));
        when(catalogCheckoutService.lockAndGetSellableVariants(List.of(7L))).thenReturn(List.of(
                new CheckoutVariantSnapshot(7L, 70L, "MacBook Air M3", "16GB - 512GB - Silver",
                        "MBA-M3-16-512", new BigDecimal("15000000.00"), 5)));
        when(couponService.evaluateForCheckout("EXPIRED", 42L, new BigDecimal("15000000.00")))
                .thenThrow(new ValidationException("Coupon has expired"));

        CreateOrderRequest request = new CreateOrderRequest(
                "Nguyen Van A", "0900000000", "Ho Chi Minh City", "EXPIRED", null, null);

        assertThrows(ValidationException.class, () -> service.createOrder(42L, request));

        verify(orders, never()).saveAndFlush(any());
        verify(couponService, never()).reserveForOrder(any(), any(), any(), any());
        verifyNoInteractions(orderItems, histories, inventoryService);
    }

    @Test
    void createOrderRejectsUnsupportedPaymentMethodBeforeTouchingCart() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .recipientName("Nguyen Van A")
                .recipientPhone("0900000000")
                .shippingAddress("Ho Chi Minh City")
                .paymentMethod("BITCOIN")
                .build();

        assertThrows(ValidationException.class, () -> service.createOrder(42L, request));

        verifyNoInteractions(cartService, catalogCheckoutService, orders, inventoryService, couponService);
    }

    @Test
    void previewCheckoutCalculatesBreakdownWithoutMutatingOrderOrCart() {
        when(cartService.getCart(42L)).thenReturn(new CartResponse(
                3L,
                List.of(new CartItemResponse(9L, 7L, "Laptop A", "SKU-7", "16GB / 512GB",
                        2, new BigDecimal("10000000.00"), new BigDecimal("20000000.00"))),
                2,
                new BigDecimal("20000000.00")
        ));
        when(catalogCheckoutService.getSellableVariants(List.of(7L))).thenReturn(List.of(
                new CheckoutVariantSnapshot(7L, 70L, "Laptop A", "16GB / 512GB", "SKU-7",
                        new BigDecimal("10000000.00"), 10)));
        when(couponService.evaluateForCheckout("SALE500K", 42L, new BigDecimal("20000000.00")))
                .thenReturn(new CouponEvaluation(2L, "SALE500K", new BigDecimal("500000.00")));

        var preview = service.previewCheckout(42L, CheckoutPreviewRequest.builder()
                .recipientName("Nguyen Van A")
                .recipientPhone("0900000000")
                .shippingAddress("123 Le Loi, Q1, TP.HCM")
                .paymentMethod("COD")
                .couponCode("sale500k")
                .build());

        assertEquals("COD", preview.getPaymentMethod());
        assertEquals("SALE500K", preview.getCouponCode());
        assertEquals(new BigDecimal("20000000.00"), preview.getSubtotal());
        assertEquals(new BigDecimal("500000.00"), preview.getDiscountAmount());
        assertEquals(BigDecimal.ZERO, preview.getShippingFee());
        assertEquals(new BigDecimal("19500000.00"), preview.getTotalAmount());
        assertEquals(2, preview.getTotalItems());

        verifyNoInteractions(orders, orderItems, histories, inventoryService);
    }

    private Order order(Long id, Long userId) {
        return Order.builder().id(id).userId(userId).orderCode("ORD-" + id).status(OrderStatus.PENDING)
                .subtotal(new BigDecimal("25000000")).discountAmount(BigDecimal.ZERO).shippingFee(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("25000000")).recipientName("Nguyen Van A").recipientPhone("0900000000")
                .shippingAddress("Ho Chi Minh City").createdAt(Instant.parse("2026-10-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-10-01T10:00:00Z")).build();
    }
}
