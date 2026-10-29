package org.akira.laptopstore.order.integration;

import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.cart.service.CartService;
import org.akira.laptopstore.cart.service.impl.CartServiceImpl;
import org.akira.laptopstore.catalog.service.impl.*;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.inventory.service.impl.InventoryServiceImpl;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.request.UpdateOrderStatusRequest;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.mapper.OrderMapper;
import org.akira.laptopstore.order.service.OrderService;
import org.akira.laptopstore.order.service.impl.*;
import org.akira.laptopstore.order.statemachine.OrderStateMachine;
import org.akira.laptopstore.payment.config.VnPayProperties;
import org.akira.laptopstore.payment.dto.request.RecordCodPaymentRequest;
import org.akira.laptopstore.payment.dto.request.StartPaymentRequest;
import org.akira.laptopstore.payment.enums.PaymentMethod;
import org.akira.laptopstore.payment.integration.VnPayGateway;
import org.akira.laptopstore.payment.service.PaymentService;
import org.akira.laptopstore.payment.service.VnPayCallbackService;
import org.akira.laptopstore.payment.service.Impl.*;
import org.akira.laptopstore.promotion.service.Impl.CouponServiceImpl;
import org.akira.laptopstore.user.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: real PostgreSQL, real proxies/transactions. Use a dedicated disposable database. */
@EnabledIfSystemProperty(named = "order.test.jdbc-url", matches = ".+")
@SpringBootTest(classes = OrderFlowPersistenceTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.config.import=", "spring.jpa.open-in-view=false", "spring.jpa.show-sql=false",
                "vnpay.tmn-code=ORDERTEST", "vnpay.hash-secret=order-test-secret-only",
                "vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
                "vnpay.return-url=http://localhost/payment/vnpay-return"})
class OrderFlowPersistenceTest {
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan("org.akira.laptopstore")
    @EnableJpaRepositories("org.akira.laptopstore")
    @EnableConfigurationProperties(VnPayProperties.class)
    @Import({CartServiceImpl.class, CatalogCartQueryServiceImpl.class, CatalogCheckoutServiceImpl.class,
            CatalogVariantQueryServiceImpl.class, InventoryServiceImpl.class, AddressServiceImpl.class,
            CouponServiceImpl.class, OrderServiceImpl.class, OrderMapper.class, OrderStateMachine.class,
            OrderPaymentServiceImpl.class, OrderQueryServiceImpl.class, PaymentImpl.class,
            PaymentTransactionServiceImpl.class, VnPayGateway.class, VnPayCallbackServiceImpl.class})
    static class TestApplication { }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> System.getProperty("order.test.jdbc-url"));
        properties.add("spring.datasource.username", () -> System.getProperty("order.test.username", "order_test"));
        properties.add("spring.datasource.password", () -> System.getProperty("order.test.password", ""));
    }
    @Autowired OrderService orders;
    @Autowired CartService carts;
    @Autowired PaymentService payments;
    @Autowired VnPayCallbackService callbacks;
    @Autowired JdbcTemplate jdbc;
    Long user, otherUser, staff, variant;
    String coupon;

    @BeforeEach void fixtures() {
        String key = UUID.randomUUID().toString();
        user = user("user-" + key); otherUser = user("other-" + key); staff = user("staff-" + key);
        Long brand = jdbc.queryForObject("insert into brands(name) values (?) returning id", Long.class, "Brand-" + key);
        Long category = jdbc.queryForObject("insert into categories(name) values (?) returning id", Long.class, "Category-" + key);
        Long product = jdbc.queryForObject("insert into products(brand_id,category_id,name) values (?,?,'Laptop test') returning id", Long.class, brand, category);
        variant = jdbc.queryForObject("insert into product_variants(product_id,sku,ram,storage,color,price,quantity) values (?,?,'16GB','512GB','Black',10000000,10) returning id", Long.class, product, "SKU-" + key);
        coupon = "TEST-" + key;
        jdbc.update("insert into coupons(code,discount_type,discount_value,usage_limit,usage_per_user) values (?,'FIXED',500000,10,2)", coupon);
        carts.addItemToCart(user, new AddCartItemRequest(variant, 2));
    }
    private Long user(String name) { return jdbc.queryForObject("insert into users(email,password_hash) values (?,'unused') returning id", Long.class, name + "@test.invalid"); }
    @AfterEach void cleanOwnOrders() {
        if (user == null || otherUser == null) return;
        jdbc.update("delete from payment_transactions where payment_id in (select p.id from payments p join orders o on o.id=p.order_id where o.user_id in (?,?))", user, otherUser);
        jdbc.update("delete from payments where order_id in (select id from orders where user_id in (?,?))", user, otherUser);
        jdbc.update("delete from coupon_usages where user_id in (?,?)", user, otherUser);
        jdbc.update("delete from orders where user_id in (?,?)", user, otherUser);
        jdbc.update("delete from carts where user_id in (?,?)", user, otherUser);
    }
    private CreateOrderRequest request(String method, String key) {
        return CreateOrderRequest.builder().recipientName("Khách kiểm thử").recipientPhone("0901234567")
                .shippingAddress("273 An Dương Vương, TP.HCM").paymentMethod(method).couponCode(coupon)
                .note("Giao giờ hành chính").idempotencyKey(key).build();
    }
    private int stock() { return jdbc.queryForObject("select quantity from product_variants where id=?", Integer.class, variant); }
    private int count(String sql, Object... args) { return jdbc.queryForObject(sql, Integer.class, args); }
    private void transition(Long id, OrderStatus status) { orders.updateOrderStatus(staff, id, new UpdateOrderStatusRequest(status, null)); }
    private Long payment(Long orderId, PaymentMethod method) {
        StartPaymentRequest request = new StartPaymentRequest(); request.setOrderId(orderId); request.setMethod(method);
        return payments.startPayment(user, request).getId();
    }

    @Test void checkoutSnapshotsTotalsAndCodDeliveryDoNotChangeStockTwice() {
        var order = orders.createOrder(user, request("COD", "cod"));
        assertEquals(new BigDecimal("19500000"), order.getTotalAmount().stripTrailingZeros().setScale(0));
        assertEquals("Giao giờ hành chính", order.getCustomerNote());
        assertEquals(8, stock()); assertTrue(carts.getCart(user).getItems().isEmpty());
        assertEquals(1, count("select count(*) from coupon_usages where order_id=? and status='ACTIVE'", order.getId()));
        Long paymentId = payment(order.getId(), PaymentMethod.COD);
        assertEquals(paymentId, payment(order.getId(), PaymentMethod.COD));
        for (var state : List.of(OrderStatus.CONFIRMED, OrderStatus.PACKING, OrderStatus.SHIPPING, OrderStatus.DELIVERED)) transition(order.getId(), state);
        RecordCodPaymentRequest cod = new RecordCodPaymentRequest(); cod.setOrderId(order.getId());
        payments.recordCodPayment(cod); payments.recordCodPayment(cod);
        assertEquals("PAID", payments.getById(user, paymentId).getStatus().name());
        assertEquals(8, stock());
        assertEquals(1, count("select count(*) from stock_movements where variant_id=? and type='SALE'", variant));
    }

    @Test void concurrentDuplicateCheckoutReturnsSameOrderAfterCartHasBeenCleared() throws Exception {
        var result = concurrent(() -> orders.createOrder(user, request("COD", "same")), () -> orders.createOrder(user, request("COD", "same")));
        assertTrue(result.stream().allMatch(OrderResponse.class::isInstance), result.toString());
        assertEquals(((OrderResponse) result.getFirst()).getId(), ((OrderResponse) result.getLast()).getId());
        assertEquals(1, count("select count(*) from orders where user_id=?", user)); assertEquals(8, stock());
    }

    @Test void codCollectionRecoversMissingPaymentInitialization() {
        var order = orders.createOrder(user, request("COD", "cod-recovery"));
        RecordCodPaymentRequest cod = new RecordCodPaymentRequest(); cod.setOrderId(order.getId());
        assertThrows(ValidationException.class, () -> payments.recordCodPayment(cod));
        for (var state : List.of(OrderStatus.CONFIRMED, OrderStatus.PACKING, OrderStatus.SHIPPING, OrderStatus.DELIVERED)) transition(order.getId(), state);
        payments.recordCodPayment(cod); payments.recordCodPayment(cod);
        assertEquals(1, count("select count(*) from payments where order_id=? and status='PAID'", order.getId()));
    }

    @Test void idempotencyIsScopedToUserAndRejectsChangedPaymentOrNote() {
        var first = orders.createOrder(user, request("COD", "shared-key"));
        assertEquals(first.getId(), orders.createOrder(user, request("COD", "shared-key")).getId());
        assertThrows(ValidationException.class, () -> orders.createOrder(user, request("VNPAY", "shared-key")));
        var changed = request("COD", "shared-key"); changed.setNote("Khác ghi chú");
        assertThrows(ValidationException.class, () -> orders.createOrder(user, changed));
        carts.addItemToCart(otherUser, new AddCartItemRequest(variant, 2));
        assertNotEquals(first.getId(), orders.createOrder(otherUser, request("COD", "shared-key")).getId());
        assertEquals(6, stock());
    }

    @Test void twoCustomersCannotOversellTheSameVariant() throws Exception {
        carts.updateCartItem(user, carts.getCart(user).getItems().getFirst().getCartItemId(), new org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest(7));
        carts.addItemToCart(otherUser, new AddCartItemRequest(variant, 7));
        var result = concurrent(() -> orders.createOrder(user, request("COD", "one")), () -> orders.createOrder(otherUser, request("COD", "two")));
        assertEquals(1, result.stream().filter(OrderResponse.class::isInstance).count(), result.toString());
        assertEquals(1, result.stream().filter(ValidationException.class::isInstance).count(), result.toString());
        assertEquals(3, stock()); assertEquals(1, count("select count(*) from stock_movements where variant_id=? and type='SALE'", variant));
    }

    @Test void checkoutRollsBackOrderStockCouponAndCartOnCleanupFailure() {
        jdbc.execute("create or replace function fail_cart_cleanup() returns trigger language plpgsql as $$ begin raise exception 'checkout cleanup failure'; end $$");
        jdbc.execute("create trigger test_checkout_failure before delete on cart_items for each row execute function fail_cart_cleanup()");
        try { assertThrows(RuntimeException.class, () -> orders.createOrder(user, request("COD", "rollback"))); }
        finally { jdbc.execute("drop trigger test_checkout_failure on cart_items"); jdbc.execute("drop function fail_cart_cleanup()"); }
        assertEquals(10, stock()); assertEquals(1, carts.getCart(user).getItems().size());
        assertEquals(0, count("select count(*) from orders where user_id=?", user));
        assertEquals(0, count("select count(*) from coupon_usages where user_id=?", user));
        assertEquals(0, count("select count(*) from stock_movements where variant_id=?", variant));
    }

    @Test void cancellationAndExpirationReleaseExactlyOnce() {
        var order = orders.createOrder(user, request("VNPAY", "expire"));
        jdbc.update("update orders set created_at=current_timestamp-interval '30 minutes' where id=?", order.getId());
        assertEquals(1, orders.expirePendingOrders(Duration.ofMinutes(15)));
        assertEquals(0, orders.expirePendingOrders(Duration.ofMinutes(15)));
        orders.cancelOrder(user, order.getId(), null);
        assertEquals(10, stock());
        assertEquals(1, count("select count(*) from stock_movements where variant_id=? and type='CANCEL_RESTORE'", variant));
        assertEquals(1, count("select count(*) from coupon_usages where order_id=? and status='RELEASED'", order.getId()));
        assertEquals(1, count("select count(*) from order_status_history where order_id=? and to_status='CANCELLED'", order.getId()));
    }

    @Test void concurrentCancelRestoresOnlyOnce() throws Exception {
        var order = orders.createOrder(user, request("COD", "cancel"));
        var result = concurrent(() -> orders.cancelOrder(user, order.getId(), null), () -> orders.cancelOrder(user, order.getId(), null));
        assertTrue(result.stream().allMatch(OrderResponse.class::isInstance), result.toString()); assertEquals(10, stock());
        assertEquals(1, count("select count(*) from stock_movements where variant_id=? and type='CANCEL_RESTORE'", variant));
    }

    @Test void cancellationFailureRollsBackStatusHistoryAndRestoration() {
        var order = orders.createOrder(user, request("COD", "restore-failure"));
        jdbc.execute("create or replace function fail_restore() returns trigger language plpgsql as $$ begin if NEW.type='CANCEL_RESTORE' then raise exception 'restore failure'; end if; return NEW; end $$");
        jdbc.execute("create trigger test_restore_failure before insert on stock_movements for each row execute function fail_restore()");
        try { assertThrows(RuntimeException.class, () -> orders.cancelOrder(user, order.getId(), null)); }
        finally { jdbc.execute("drop trigger test_restore_failure on stock_movements"); jdbc.execute("drop function fail_restore()"); }
        assertEquals(OrderStatus.WAITING_CONFIRMATION, orders.getOwnOrder(user, order.getId()).getStatus()); assertEquals(8, stock());
        assertEquals(1, count("select count(*) from order_status_history where order_id=?", order.getId()));
        assertEquals(1, count("select count(*) from coupon_usages where order_id=? and status='ACTIVE'", order.getId()));
    }

    @Test void deliveryRetryLimitPreventsFourthAttemptAndCancelRestoresOnce() {
        var order = orders.createOrder(user, request("COD", "retry"));
        transition(order.getId(), OrderStatus.CONFIRMED); transition(order.getId(), OrderStatus.PACKING); transition(order.getId(), OrderStatus.SHIPPING);
        for (int attempt = 0; attempt < 3; attempt++) { transition(order.getId(), OrderStatus.DELIVERY_FAILED); if (attempt < 2) transition(order.getId(), OrderStatus.SHIPPING); }
        assertThrows(ValidationException.class, () -> transition(order.getId(), OrderStatus.SHIPPING));
        assertEquals(List.of(OrderStatus.CANCELLED), orders.getStaffOrder(order.getId()).getAllowedTransitions());
        assertEquals(8, stock()); transition(order.getId(), OrderStatus.CANCELLED); transition(order.getId(), OrderStatus.CANCELLED); assertEquals(10, stock());
    }

    @Test void expirationFailureRollsBackCancellationAndCanBeRetried() {
        var order = orders.createOrder(user, request("VNPAY", "expire-rollback"));
        jdbc.update("update orders set created_at=current_timestamp-interval '30 minutes' where id=?", order.getId());
        jdbc.execute("create or replace function fail_expire_restore() returns trigger language plpgsql as $$ begin if NEW.type='CANCEL_RESTORE' then raise exception 'expiration restore failure'; end if; return NEW; end $$");
        jdbc.execute("create trigger test_expire_failure before insert on stock_movements for each row execute function fail_expire_restore()");
        try { assertThrows(RuntimeException.class, () -> orders.expirePendingOrders()); }
        finally { jdbc.execute("drop trigger test_expire_failure on stock_movements"); jdbc.execute("drop function fail_expire_restore()"); }
        assertEquals(OrderStatus.PENDING_PAYMENT, orders.getOwnOrder(user, order.getId()).getStatus());
        assertEquals(8, stock());
        assertEquals(1, count("select count(*) from order_status_history where order_id=?", order.getId()));
        assertEquals(1, orders.expirePendingOrders()); assertEquals(10, stock());
    }

    @Test void verifiedIpnConfirmsOnceAndPaidOnlineOrderCannotBeCancelled() throws Exception {
        var order = orders.createOrder(user, request("VNPAY", "online")); Long id = payment(order.getId(), PaymentMethod.VNPAY);
        var session = payments.createVnpaySession(user, id, "127.0.0.1");
        assertEquals(session.getExpiresAt(), payments.createVnpaySession(user, id, "127.0.0.1").getExpiresAt());
        var callback = callback(id, true); callbacks.handleIpn(callback); callbacks.handleIpn(callback);
        callbacks.handleIpn(callback(id, false));
        assertEquals(OrderStatus.CONFIRMED, orders.getOwnOrder(user, order.getId()).getStatus());
        assertEquals("PAID", payments.getById(user, id).getStatus().name());
        assertEquals(1, count("select count(*) from order_status_history where order_id=? and to_status='CONFIRMED'", order.getId()));
        assertThrows(ValidationException.class, () -> transition(order.getId(), OrderStatus.CANCELLED)); assertEquals(8, stock());
    }

    @Test void paymentSuccessAfterCancellationIsRetainedForReconciliation() throws Exception {
        var order = orders.createOrder(user, request("VNPAY", "late")); Long id = payment(order.getId(), PaymentMethod.VNPAY);
        payments.createVnpaySession(user, id, "127.0.0.1"); orders.cancelOrder(user, order.getId(), null);
        callbacks.handleIpn(callback(id, true));
        assertEquals("REQUIRES_RECONCILIATION", payments.getById(user, id).getStatus().name());
        assertEquals(OrderStatus.CANCELLED, orders.getOwnOrder(user, order.getId()).getStatus()); assertEquals(10, stock());
        assertEquals(1, count("select count(*) from payment_transactions where payment_id=? and status='SUCCESS'", id));
    }

    @Test void failedOnlinePaymentCanRetryWithoutAnotherOrderOrStockDeduction() throws Exception {
        var order = orders.createOrder(user, request("VNPAY", "payment-retry")); Long id = payment(order.getId(), PaymentMethod.VNPAY);
        payments.createVnpaySession(user, id, "127.0.0.1"); callbacks.handleIpn(callback(id, false));
        assertEquals("FAILED", payments.getById(user, id).getStatus().name());
        payments.createVnpaySession(user, id, "127.0.0.1"); callbacks.handleIpn(callback(id, true));
        assertEquals(OrderStatus.CONFIRMED, orders.getOwnOrder(user, order.getId()).getStatus());
        assertEquals(2, count("select count(*) from payment_transactions where payment_id=?", id));
        assertEquals(1, count("select count(*) from orders where user_id=?", user)); assertEquals(8, stock());
    }

    @Test void paymentAndExpirationRaceHasOneConsistentOutcome() throws Exception {
        var order = orders.createOrder(user, request("VNPAY", "payment-expire-race")); Long id = payment(order.getId(), PaymentMethod.VNPAY);
        payments.createVnpaySession(user, id, "127.0.0.1");
        jdbc.update("update orders set created_at=current_timestamp-interval '30 minutes' where id=?", order.getId());
        var callback = callback(id, true);
        var result = concurrent(() -> orders.expirePendingOrders(), () -> callbacks.handleIpn(callback));
        assertTrue(result.stream().noneMatch(Throwable.class::isInstance), result.toString());
        var status = orders.getOwnOrder(user, order.getId()).getStatus();
        if (status == OrderStatus.CANCELLED) { assertEquals(10, stock()); assertEquals("REQUIRES_RECONCILIATION", payments.getById(user, id).getStatus().name()); }
        else { assertEquals(OrderStatus.CONFIRMED, status); assertEquals(8, stock()); assertEquals("PAID", payments.getById(user, id).getStatus().name()); }
    }

    @Test void invalidSignatureOrAmountDoesNotConfirmPayment() throws Exception {
        var order = orders.createOrder(user, request("VNPAY", "tamper")); Long id = payment(order.getId(), PaymentMethod.VNPAY);
        payments.createVnpaySession(user, id, "127.0.0.1");
        var callback = callback(id, true); callback.put("vnp_SecureHash", "bad"); callbacks.handleIpn(callback);
        callback = callback(id, true); callback.put("vnp_Amount", "100"); sign(callback); callbacks.handleIpn(callback);
        assertEquals("PENDING", payments.getById(user, id).getStatus().name());
        assertEquals(OrderStatus.PENDING_PAYMENT, orders.getOwnOrder(user, order.getId()).getStatus());
    }

    @Test void orderSearchNeverIncludesOtherCustomers() {
        var mine = orders.createOrder(user, request("COD", "search"));
        carts.addItemToCart(otherUser, new AddCartItemRequest(variant, 1)); orders.createOrder(otherUser, request("COD", "search"));
        var page = orders.searchOrders(user, OrderStatus.WAITING_CONFIRMATION, "", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements()); assertEquals(mine.getId(), page.getContent().getFirst().getId());
    }

    private Map<String, String> callback(Long paymentId, boolean success) throws Exception {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_TmnCode", "ORDERTEST");
        params.put("vnp_TxnRef", jdbc.queryForObject("select provider_reference from payment_transactions where payment_id=? order by id desc limit 1", String.class, paymentId));
        params.put("vnp_Amount", payments.getById(user, paymentId).getAmount().movePointRight(2).toBigIntegerExact().toString());
        params.put("vnp_ResponseCode", success ? "00" : "24"); params.put("vnp_TransactionStatus", success ? "00" : "02");
        Long transactionId = jdbc.queryForObject("select max(id) from payment_transactions where payment_id=?", Long.class, paymentId);
        params.put("vnp_TransactionNo", String.valueOf(9000000000000L + transactionId)); sign(params); return params;
    }
    private void sign(Map<String, String> params) throws Exception {
        params.remove("vnp_SecureHash");
        String data = new TreeMap<>(params).entrySet().stream().map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII)).collect(Collectors.joining("&"));
        Mac mac = Mac.getInstance("HmacSHA512"); mac.init(new SecretKeySpec("order-test-secret-only".getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        params.put("vnp_SecureHash", HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8))));
    }
    private List<Object> concurrent(Callable<?> first, Callable<?> second) throws Exception {
        var barrier = new CyclicBarrier(2);
        try (var workers = Executors.newFixedThreadPool(2)) {
            List<Future<Object>> futures = new ArrayList<>();
            for (var task : List.of(first, second)) futures.add(workers.submit(() -> { barrier.await(5, TimeUnit.SECONDS); try { return task.call(); } catch (RuntimeException exception) { return exception; } }));
            List<Object> results = new ArrayList<>(); for (var future : futures) results.add(future.get(15, TimeUnit.SECONDS)); return results;
        }
    }
}
