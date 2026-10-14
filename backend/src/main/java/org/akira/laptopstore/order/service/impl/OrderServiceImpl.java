package org.akira.laptopstore.order.service.impl;

import org.akira.laptopstore.cart.dto.response.CartItemResponse;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.dto.response.CartSnapshot;
import org.akira.laptopstore.cart.service.CartService;
import org.akira.laptopstore.catalog.dto.response.CheckoutVariantSnapshot;
import org.akira.laptopstore.catalog.service.CatalogCheckoutService;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.UnauthenticatedException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.inventory.dto.request.OrderStockDeduction;
import org.akira.laptopstore.inventory.service.InventoryService;
import org.akira.laptopstore.order.dto.request.CheckoutPreviewRequest;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.response.CheckoutPreviewResponse;
import org.akira.laptopstore.order.dto.response.OrderItemResponse;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.OrderItem;
import org.akira.laptopstore.order.entity.OrderStatusHistory;
import org.akira.laptopstore.order.entity.enums.CheckoutPaymentMethod;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.mapper.OrderMapper;
import org.akira.laptopstore.order.repository.OrderItemRepository;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.repository.OrderStatusHistoryRepository;
import org.akira.laptopstore.order.service.OrderService;
import org.akira.laptopstore.order.util.OrderCodeGenerator;
import org.akira.laptopstore.promotion.dto.response.CouponEvaluation;
import org.akira.laptopstore.promotion.service.CouponService;
import org.akira.laptopstore.user.dto.AddressResponse;
import org.akira.laptopstore.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final CatalogCheckoutService catalogCheckoutService;
    private final InventoryService inventoryService;
    private final AddressService addressService;
    private final CouponService couponService;

    @Autowired
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            OrderMapper orderMapper,
            CartService cartService,
            CatalogCheckoutService catalogCheckoutService,
            InventoryService inventoryService,
            AddressService addressService,
            CouponService couponService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.orderMapper = orderMapper;
        this.cartService = cartService;
        this.catalogCheckoutService = catalogCheckoutService;
        this.inventoryService = inventoryService;
        this.addressService = addressService;
        this.couponService = couponService;
    }

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            OrderMapper orderMapper,
            CartService cartService,
            CatalogCheckoutService catalogCheckoutService,
            InventoryService inventoryService,
            CouponService couponService
    ) {
        this(orderRepository, orderItemRepository, orderStatusHistoryRepository, orderMapper,
                cartService, catalogCheckoutService, inventoryService, null, couponService);
    }

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            OrderMapper orderMapper,
            CartService cartService,
            CatalogCheckoutService catalogCheckoutService,
            InventoryService inventoryService
    ) {
        this(orderRepository, orderItemRepository, orderStatusHistoryRepository, orderMapper,
                cartService, catalogCheckoutService, inventoryService, null, null);
    }

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusHistoryRepository orderStatusHistoryRepository,
            OrderMapper orderMapper
    ) {
        this(orderRepository, orderItemRepository, orderStatusHistoryRepository, orderMapper,
                null, null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutPreviewResponse previewCheckout(Long userId, CheckoutPreviewRequest request) {
        requireUserId(userId);
        CheckoutPreviewRequest safeRequest = request != null ? request : new CheckoutPreviewRequest();

        CheckoutPaymentMethod paymentMethod = CheckoutPaymentMethod.fromValue(safeRequest.getPaymentMethod());
        ResolvedShippingAddress shipping = resolvePreviewShippingAddress(userId, safeRequest);

        if (cartService == null || catalogCheckoutService == null) {
            throw new IllegalStateException("Checkout dependencies are not configured");
        }

        CartResponse cart = cartService.getCart(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ValidationException("Cart is empty");
        }

        List<CartSnapshot.Item> cartItems = cart.getItems().stream()
                .map(item -> new CartSnapshot.Item(item.getCartItemId(), item.getVariantId(), item.getQuantity()))
                .toList();

        Map<Long, CheckoutVariantSnapshot> variants = catalogCheckoutService
                .getSellableVariants(cartItems.stream().map(CartSnapshot.Item::getVariantId).toList())
                .stream()
                .collect(Collectors.toMap(CheckoutVariantSnapshot::getVariantId, variant -> variant));

        validateStockBeforeOrder(cartItems, variants);

        BigDecimal subtotal = calculateSubtotal(cartItems, variants);
        BigDecimal shippingFee = calculateShippingFee(subtotal);
        BigDecimal discount = BigDecimal.ZERO;
        String appliedCouponCode = null;

        String requestedCouponCode = normalizeUpper(safeRequest.getCouponCode());
        if (requestedCouponCode != null) {
            if (couponService == null) {
                throw new ValidationException("Coupon checkout is not available yet");
            }
            CouponEvaluation evaluation = couponService.evaluateForCheckout(requestedCouponCode, userId, subtotal);
            if (evaluation != null) {
                discount = normalizeDiscount(evaluation.discountAmount(), subtotal);
                appliedCouponCode = evaluation.couponCode() != null
                        ? evaluation.couponCode().trim().toUpperCase(Locale.ROOT)
                        : requestedCouponCode;
            }
        }

        BigDecimal totalAmount = calculateTotalAmount(subtotal, discount, shippingFee);

        List<OrderItemResponse> previewItems = new ArrayList<>(cartItems.size());
        int totalItems = 0;
        for (CartItemResponse item : cart.getItems()) {
            CheckoutVariantSnapshot variant = requireVariant(variants, item.getVariantId());
            BigDecimal lineTotal = variant.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalItems += item.getQuantity();
            previewItems.add(new OrderItemResponse(
                    null,
                    variant.getProductId(),
                    variant.getVariantId(),
                    variant.getProductName(),
                    variant.getVariantName(),
                    variant.getSku(),
                    variant.getUnitPrice(),
                    item.getQuantity(),
                    lineTotal
            ));
        }

        return CheckoutPreviewResponse.builder()
                .addressId(shipping.addressId())
                .recipientName(shipping.recipientName())
                .recipientPhone(shipping.recipientPhone())
                .shippingAddress(shipping.shippingAddress())
                .paymentMethod(paymentMethod.name())
                .couponCode(appliedCouponCode)
                .subtotal(subtotal)
                .discountAmount(discount)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .items(List.copyOf(previewItems))
                .build();
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        requireUserId(userId);
        if (request == null) {
            throw new ValidationException("Checkout request is required");
        }

        // 1. Validate phương thức thanh toán và địa chỉ giao hàng trước khi tạo order.
        CheckoutPaymentMethod.fromValue(request.getPaymentMethod());
        ResolvedShippingAddress resolvedShipping = resolveRequiredShippingAddress(userId, request);
        String requestedCouponCode = normalizeUpper(request.getCouponCode());

        // 2. Kiểm tra Idempotency-Key chống tạo đơn trùng.
        String idempotencyKey = normalize(request.getIdempotencyKey());
        if (idempotencyKey != null) {
            Order previous = orderRepository.findByIdempotencyKeyAndUserId(idempotencyKey, userId).orElse(null);
            if (previous != null) {
                if (!sameCheckoutPayload(previous, resolvedShipping, requestedCouponCode)) {
                    throw new ValidationException("Idempotency-Key was already used with a different checkout payload");
                }
                return orderMapper.toResponse(
                        previous,
                        orderItemRepository.findByOrderId(previous.getId()),
                        orderStatusHistoryRepository.findByOrderIdOrderByChangedAtDesc(previous.getId())
                );
            }
        }

        requireCheckoutDependencies();

        // 3. Khóa cart để dữ liệu checkout không bị thay đổi đồng thời.
        CartSnapshot cart = cartService.lockAndGetCheckoutCart(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ValidationException("Cart is empty");
        }

        // 4. Khóa variant theo ID tăng dần, xác minh còn bán và lấy snapshot giá/tồn hiện tại.
        Map<Long, CheckoutVariantSnapshot> variants = catalogCheckoutService
                .lockAndGetSellableVariants(cart.getItems().stream()
                        .map(CartSnapshot.Item::getVariantId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(CheckoutVariantSnapshot::getVariantId, variant -> variant));

        // 5. Validate tồn kho của từng biến thể trước khi tạo order.
        validateStockBeforeOrder(cart.getItems(), variants);

        // 6. Tính lại subtotal/discount/shipping/total ở server, validate coupon trước khi tạo order.
        BigDecimal subtotal = calculateSubtotal(cart.getItems(), variants);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal shippingFee = calculateShippingFee(subtotal);
        String couponCodeSnapshot = null;

        if (requestedCouponCode != null) {
            if (couponService == null) {
                throw new ValidationException("Coupon checkout is not available yet");
            }
            CouponEvaluation preEvaluation = couponService.evaluateForCheckout(requestedCouponCode, userId, subtotal);
            if (preEvaluation != null) {
                discount = normalizeDiscount(preEvaluation.discountAmount(), subtotal);
                couponCodeSnapshot = preEvaluation.couponCode() != null
                        ? preEvaluation.couponCode().trim().toUpperCase(Locale.ROOT)
                        : requestedCouponCode;
            }
        }

        BigDecimal totalAmount = calculateTotalAmount(subtotal, discount, shippingFee);

        // 7. Lưu order để có ID cho item, coupon usage, lịch sử trạng thái và lịch sử kho.
        Order order = orderRepository.saveAndFlush(Order.builder()
                .userId(userId)
                .orderCode(OrderCodeGenerator.generate())
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .discountAmount(discount)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .recipientName(resolvedShipping.recipientName())
                .recipientPhone(resolvedShipping.recipientPhone())
                .shippingAddress(resolvedShipping.shippingAddress())
                .couponCodeSnapshot(couponCodeSnapshot)
                .idempotencyKey(idempotencyKey)
                .build());

        // 8. Khóa và ghi nhận lượt sử dụng coupon gắn với orderId trong cùng transaction.
        if (requestedCouponCode != null) {
            CouponEvaluation reservedEvaluation = couponService.reserveForOrder(
                    requestedCouponCode,
                    userId,
                    order.getId(),
                    subtotal
            );
            if (reservedEvaluation != null) {
                BigDecimal reservedDiscount = normalizeDiscount(reservedEvaluation.discountAmount(), subtotal);
                String reservedCode = reservedEvaluation.couponCode() != null
                        ? reservedEvaluation.couponCode().trim().toUpperCase(Locale.ROOT)
                        : requestedCouponCode;
                BigDecimal reservedTotal = calculateTotalAmount(subtotal, reservedDiscount, shippingFee);

                if (order.getDiscountAmount().compareTo(reservedDiscount) != 0
                        || order.getTotalAmount().compareTo(reservedTotal) != 0
                        || !Objects.equals(order.getCouponCodeSnapshot(), reservedCode)) {
                    order.setDiscountAmount(reservedDiscount);
                    order.setTotalAmount(reservedTotal);
                    order.setCouponCodeSnapshot(reservedCode);
                    orderRepository.save(order);
                }
            }
        }

        // 9. Lưu snapshot sản phẩm để dữ liệu đơn không đổi khi catalog được cập nhật.
        List<OrderItem> items = cart.getItems().stream().map(cartItem -> {
            CheckoutVariantSnapshot variant = requireVariant(variants, cartItem.getVariantId());
            return OrderItem.builder()
                    .order(order)
                    .productId(variant.getProductId())
                    .variantId(variant.getVariantId())
                    .productName(variant.getProductName())
                    .variantName(variant.getVariantName())
                    .sku(variant.getSku())
                    .unitPrice(variant.getUnitPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
        }).toList();
        orderItemRepository.saveAll(items);

        // 10. Đơn mới luôn bắt đầu ở trạng thái PENDING.
        OrderStatusHistory history = orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .toStatus(OrderStatus.PENDING)
                .changedAt(Instant.now())
                .changedByUserId(userId)
                .build());

        // 11. Trừ tồn và ghi stock movement; lỗi sẽ rollback toàn bộ checkout.
        inventoryService.deductForOrder(
                order.getId(),
                userId,
                cart.getItems().stream()
                        .map(item -> new OrderStockDeduction(item.getVariantId(), item.getQuantity()))
                        .toList()
        );

        // 12. Chỉ xóa cart sau khi đã tạo order và trừ tồn thành công.
        cartService.clearPurchasedItems(
                userId,
                cart.getItems().stream().map(CartSnapshot.Item::getCartItemId).toList()
        );

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
        return orderMapper.toResponse(
                order,
                orderItemRepository.findByOrderId(orderId),
                orderStatusHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId)
        );
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
                .mapToInt(OrderItem::getQuantity)
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

    private ResolvedShippingAddress resolveRequiredShippingAddress(Long userId, CreateOrderRequest request) {
        Long addressId = request.getAddressId();
        if (addressId != null) {
            if (addressId <= 0) {
                throw new ValidationException("addressId must be greater than 0");
            }
            if (addressService == null) {
                throw new IllegalStateException("AddressService is not configured");
            }
            AddressResponse ownedAddress = addressService.getAddressById(userId, addressId);
            String recipientName = !isBlank(request.getRecipientName())
                    ? request.getRecipientName().trim()
                    : ownedAddress.getRecipientName().trim();
            String recipientPhone = !isBlank(request.getRecipientPhone())
                    ? request.getRecipientPhone().trim()
                    : ownedAddress.getPhone().trim();
            String shippingAddress = !isBlank(request.getShippingAddress())
                    ? request.getShippingAddress().trim()
                    : formatAddress(ownedAddress);
            validateShippingStrings(recipientName, recipientPhone, shippingAddress);
            return new ResolvedShippingAddress(addressId, recipientName, recipientPhone, shippingAddress);
        }

        if (isBlank(request.getRecipientName())
                || isBlank(request.getRecipientPhone())
                || isBlank(request.getShippingAddress())) {
            throw new ValidationException("Recipient name, phone and shipping address are required");
        }
        String recipientName = request.getRecipientName().trim();
        String recipientPhone = request.getRecipientPhone().trim();
        String shippingAddress = request.getShippingAddress().trim();
        validateShippingStrings(recipientName, recipientPhone, shippingAddress);
        return new ResolvedShippingAddress(null, recipientName, recipientPhone, shippingAddress);
    }

    private ResolvedShippingAddress resolvePreviewShippingAddress(Long userId, CheckoutPreviewRequest request) {
        Long addressId = request.getAddressId();
        if (addressId != null) {
            if (addressId <= 0) {
                throw new ValidationException("addressId must be greater than 0");
            }
            if (addressService == null) {
                throw new IllegalStateException("AddressService is not configured");
            }
            AddressResponse ownedAddress = addressService.getAddressById(userId, addressId);
            String recipientName = !isBlank(request.getRecipientName())
                    ? request.getRecipientName().trim()
                    : ownedAddress.getRecipientName().trim();
            String recipientPhone = !isBlank(request.getRecipientPhone())
                    ? request.getRecipientPhone().trim()
                    : ownedAddress.getPhone().trim();
            String shippingAddress = !isBlank(request.getShippingAddress())
                    ? request.getShippingAddress().trim()
                    : formatAddress(ownedAddress);
            return new ResolvedShippingAddress(addressId, recipientName, recipientPhone, shippingAddress);
        }

        return new ResolvedShippingAddress(
                null,
                normalize(request.getRecipientName()),
                normalize(request.getRecipientPhone()),
                normalize(request.getShippingAddress())
        );
    }

    private void validateShippingStrings(String recipientName, String recipientPhone, String shippingAddress) {
        if (isBlank(recipientName) || isBlank(recipientPhone) || isBlank(shippingAddress)) {
            throw new ValidationException("Recipient name, phone and shipping address are required");
        }
        if (recipientName.length() > 100) {
            throw new ValidationException("Recipient name must not exceed 100 characters");
        }
        if (recipientPhone.length() > 20) {
            throw new ValidationException("Recipient phone must not exceed 20 characters");
        }
    }

    private String formatAddress(AddressResponse address) {
        List<String> parts = new ArrayList<>(5);
        if (!isBlank(address.getStreet())) {
            parts.add(address.getStreet().trim());
        }
        if (!isBlank(address.getWard())) {
            parts.add(address.getWard().trim());
        }
        if (!isBlank(address.getDistrict())) {
            parts.add(address.getDistrict().trim());
        }
        if (!isBlank(address.getCity())) {
            parts.add(address.getCity().trim());
        }
        if (!isBlank(address.getPostalCode())) {
            parts.add(address.getPostalCode().trim());
        }
        return String.join(", ", parts);
    }

    private void validateStockBeforeOrder(
            List<CartSnapshot.Item> cartItems,
            Map<Long, CheckoutVariantSnapshot> variants
    ) {
        Map<Long, Integer> requestedByVariant = new LinkedHashMap<>();
        for (CartSnapshot.Item item : cartItems) {
            if (item == null || item.getVariantId() == null || item.getVariantId() <= 0
                    || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ValidationException("Cart items must contain a positive variantId and quantity");
            }
            CheckoutVariantSnapshot variant = requireVariant(variants, item.getVariantId());
            if (variant.getUnitPrice() == null || variant.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Invalid unit price for product variant: " + item.getVariantId());
            }
            int updatedQty = Math.addExact(
                    requestedByVariant.getOrDefault(item.getVariantId(), 0),
                    item.getQuantity()
            );
            requestedByVariant.put(item.getVariantId(), updatedQty);
        }

        for (Map.Entry<Long, Integer> entry : requestedByVariant.entrySet()) {
            CheckoutVariantSnapshot variant = requireVariant(variants, entry.getKey());
            if (variant.getAvailableQuantity() != null && entry.getValue() > variant.getAvailableQuantity()) {
                throw new ValidationException("Not enough stock for product variant: " + entry.getKey());
            }
        }
    }

    private BigDecimal calculateSubtotal(
            List<CartSnapshot.Item> cartItems,
            Map<Long, CheckoutVariantSnapshot> variants
    ) {
        return cartItems.stream()
                .map(item -> requireVariant(variants, item.getVariantId())
                        .getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateShippingFee(BigDecimal subtotal) {
        return BigDecimal.ZERO;
    }

    private BigDecimal normalizeDiscount(BigDecimal rawDiscount, BigDecimal subtotal) {
        if (rawDiscount == null || rawDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (rawDiscount.compareTo(subtotal) > 0) {
            return subtotal;
        }
        return rawDiscount;
    }

    private BigDecimal calculateTotalAmount(BigDecimal subtotal, BigDecimal discount, BigDecimal shippingFee) {
        BigDecimal total = subtotal.subtract(discount).add(shippingFee);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Order total amount cannot be negative");
        }
        return total;
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

    private boolean sameCheckoutPayload(
            Order order,
            ResolvedShippingAddress resolvedShipping,
            String requestedCouponCode
    ) {
        return Objects.equals(order.getRecipientName(), resolvedShipping.recipientName())
                && Objects.equals(order.getRecipientPhone(), resolvedShipping.recipientPhone())
                && Objects.equals(order.getShippingAddress(), resolvedShipping.shippingAddress())
                && Objects.equals(normalizeUpper(order.getCouponCodeSnapshot()), requestedCouponCode);
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String normalizeUpper(String value) {
        return isBlank(value) ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ResolvedShippingAddress(
            Long addressId,
            String recipientName,
            String recipientPhone,
            String shippingAddress
    ) {
    }
}
