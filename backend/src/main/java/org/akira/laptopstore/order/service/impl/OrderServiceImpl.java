package org.akira.laptopstore.order.service.impl;

import org.akira.laptopstore.cart.dto.response.CartSnapshot;
import org.akira.laptopstore.cart.service.CartService;
import org.akira.laptopstore.catalog.dto.response.CheckoutVariantSnapshot;
import org.akira.laptopstore.catalog.service.CatalogCheckoutService;
import org.akira.laptopstore.inventory.dto.request.OrderStockDeduction;
import org.akira.laptopstore.inventory.service.InventoryService;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.OrderItem;
import org.akira.laptopstore.order.entity.OrderStatusHistory;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.UnauthenticatedException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.order.mapper.OrderMapper;
import org.akira.laptopstore.order.repository.OrderItemRepository;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.repository.OrderStatusHistoryRepository;
import org.akira.laptopstore.order.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor 
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final CatalogCheckoutService catalogCheckoutService;
    private final InventoryService inventoryService;


    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        requireUserId(userId);
        requireCheckoutRequest(request);

        String idempotencyKey = normalize(request.getIdempotencyKey());
        if (idempotencyKey != null) {
            Order previous = orderRepository.findByIdempotencyKeyAndUserId(idempotencyKey, userId).orElse(null);
            if (previous != null) {
                if (!sameCheckoutPayload(previous, request)) {
                    throw new ValidationException("Idempotency-Key was already used with a different checkout payload");
                }
                return orderMapper.toResponse(previous, orderItemRepository.findByOrderId(previous.getId()),
                        orderStatusHistoryRepository.findByOrderIdOrderByChangedAtDesc(previous.getId()));
            }
        }

        requireCheckoutDependencies();
        CartSnapshot cart = cartService.lockAndGetCheckoutCart(userId);
        Map<Long, CheckoutVariantSnapshot> variants = catalogCheckoutService
                .lockAndGetSellableVariants(cart.getItems().stream().map(CartSnapshot.Item::getVariantId).toList())
                .stream().collect(Collectors.toMap(CheckoutVariantSnapshot::getVariantId, Function.identity()));

        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> requireVariant(variants, item.getVariantId()).getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal shippingFee = BigDecimal.ZERO;
        Order order = orderRepository.saveAndFlush(Order.builder()
                .userId(userId).orderCode("ORD-" + UUID.randomUUID()).status(OrderStatus.PENDING).subtotal(subtotal)
                .discountAmount(discount).shippingFee(shippingFee).totalAmount(subtotal)
                .recipientName(request.getRecipientName().trim()).recipientPhone(request.getRecipientPhone().trim())
                .shippingAddress(request.getShippingAddress().trim()).couponCodeSnapshot(null)
                .idempotencyKey(idempotencyKey).build());
        List<OrderItem> items = cart.getItems().stream().map(cartItem -> {
            CheckoutVariantSnapshot variant = requireVariant(variants, cartItem.getVariantId());
            return OrderItem.builder().order(order).productId(variant.getProductId()).variantId(variant.getVariantId())
                    .productName(variant.getProductName()).variantName(variant.getVariantName()).sku(variant.getSku())
                    .unitPrice(variant.getUnitPrice()).quantity(cartItem.getQuantity()).build();
        }).toList();
        orderItemRepository.saveAll(items);
        OrderStatusHistory history = orderStatusHistoryRepository.save(OrderStatusHistory.builder().order(order)
                .toStatus(OrderStatus.PENDING).changedAt(Instant.now()).changedByUserId(userId).build());
        inventoryService.deductForOrder(order.getId(), userId, cart.getItems().stream()
                .map(item -> new OrderStockDeduction(item.getVariantId(), item.getQuantity())).toList());
        cartService.clearPurchasedItems(userId, cart.getItems().stream().map(CartSnapshot.Item::getCartItemId).toList());

        return orderMapper.toResponse(order, items, List.of(history));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOwnOrder(Long userId, Long orderId) {
        requireUserId(userId);
        requireOrderId(orderId);
        Order order = orderRepository.findById(orderId)
                .filter(candidate -> userId.equals(candidate.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderMapper.toResponse(order, orderItemRepository.findByOrderId(orderId),
                orderStatusHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOwnOrders(Long userId, Pageable pageable) {
        requireUserId(userId);
        return orderRepository.findByUserId(userId, pageable).map(this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toSummaryResponse);
    }

    private OrderSummaryResponse toSummaryResponse(Order order) {
        int totalItems = orderItemRepository.findByOrderId(order.getId()).stream()
                .mapToInt(item -> item.getQuantity())
                .sum();
        return orderMapper.toSummaryResponse(order, totalItems);
    }

    private void requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new UnauthenticatedException("Invalid authenticated user");
        }
    }

    private void requireOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId must be greater than 0");
        }
    }

    private void requireCheckoutRequest(CreateOrderRequest request) {
        if (request == null || isBlank(request.getRecipientName()) || isBlank(request.getRecipientPhone())
                || isBlank(request.getShippingAddress())) {
            throw new ValidationException("Recipient name, phone and shipping address are required");
        }
        if (!isBlank(request.getCouponCode())) {
            throw new ValidationException("Coupon checkout is not available yet");
        }
    }

    private void requireCheckoutDependencies() {
        if (cartService == null || catalogCheckoutService == null || inventoryService == null) {
            throw new IllegalStateException("Checkout dependencies are not configured");
        }
    }

    private CheckoutVariantSnapshot requireVariant(Map<Long, CheckoutVariantSnapshot> variants, Long variantId) {
        CheckoutVariantSnapshot variant = variants.get(variantId);
        if (variant == null) {
            throw new ResourceNotFoundException("Product variant not found: " + variantId);
        }
        return variant;
    }

    private boolean sameCheckoutPayload(Order order, CreateOrderRequest request) {
        return order.getRecipientName().equals(request.getRecipientName().trim())
                && order.getRecipientPhone().equals(request.getRecipientPhone().trim())
                && order.getShippingAddress().equals(request.getShippingAddress().trim());
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
