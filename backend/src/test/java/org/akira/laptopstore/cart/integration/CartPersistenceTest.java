package org.akira.laptopstore.cart.integration;

import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest;
import org.akira.laptopstore.cart.service.CartService;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.cart.repository.CartRepository;
import org.akira.laptopstore.cart.service.impl.CartServiceImpl;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in tests against a dedicated, disposable PostgreSQL database, never the development DB. */
@EnabledIfSystemProperty(named = "cart.test.jdbc-url", matches = ".+")
@SpringBootTest(classes = CartPersistenceTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.jpa.open-in-view=false", "spring.jpa.show-sql=false"})
class CartPersistenceTest {
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {Cart.class, ProductVariant.class})
    @EnableJpaRepositories(basePackageClasses = {CartRepository.class, ProductVariantRepository.class})
    @Import(CartServiceImpl.class)
    static class TestApplication {
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> System.getProperty("cart.test.jdbc-url"));
        properties.add("spring.datasource.username", () -> System.getProperty("cart.test.username", "cart_test"));
        properties.add("spring.datasource.password", () -> System.getProperty("cart.test.password", ""));
    }

    @Autowired private CartService cartService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;
    private Long userId;
    private Long variantId;
    private Long otherVariantId;

    @BeforeEach
    void fixtures() {
        String suffix = UUID.randomUUID().toString();
        userId = jdbc.queryForObject("insert into users (email, password_hash) values (?, 'unused') returning id",
                Long.class, "cart-" + suffix + "@example.com");
        Long brandId = jdbc.queryForObject("insert into brands (name) values (?) returning id", Long.class, "Brand-" + suffix);
        Long categoryId = jdbc.queryForObject("insert into categories (name) values (?) returning id", Long.class, "Category-" + suffix);
        Long productId = jdbc.queryForObject("insert into products (brand_id, category_id, name) values (?, ?, 'Laptop') returning id",
                Long.class, brandId, categoryId);
        variantId = createVariant(productId, "SKU-" + suffix);
        otherVariantId = createVariant(productId, "OTHER-" + suffix);
    }

    @Test
    void crudPersistsItemIdsCurrentPricesAndOrphanRemovalWithoutChangingStock() {
        var added = cartService.addItemToCart(userId, new AddCartItemRequest(variantId, 2));
        Long itemId = added.getItems().getFirst().getCartItemId();
        assertNotNull(itemId);
        assertEquals(new BigDecimal("201.00"), added.getTotalAmount());
        assertEquals("Laptop", added.getItems().getFirst().getProductName());
        assertEquals("Laptop (16GB - 512GB - Silver)", added.getItems().getFirst().getVariantName());

        var updated = cartService.updateCartItem(userId, itemId, new UpdateCartItemRequest(3));
        assertEquals(new BigDecimal("301.50"), updated.getTotalAmount());
        jdbc.update("update product_variants set price = 200 where id = ?", variantId);
        assertEquals(new BigDecimal("600.00"), cartService.getCart(userId).getTotalAmount());
        cartService.removeCartItem(userId, itemId);
        assertEquals(0, jdbc.queryForObject("select count(*) from cart_items where id = ?", Integer.class, itemId));
        assertTrue(cartService.getCart(userId).getItems().isEmpty());

        cartService.addItemToCart(userId, new AddCartItemRequest(variantId, 1));
        cartService.addItemToCart(userId, new AddCartItemRequest(otherVariantId, 1));
        cartService.clearCart(userId);
        assertTrue(cartService.getCart(userId).getItems().isEmpty());
        assertEquals(20, jdbc.queryForObject("select quantity from product_variants where id = ?", Integer.class, variantId));
    }

    @Test
    void concurrentFirstAddsCreateOneCartAndOneMergedLine() throws Exception {
        var start = new CyclicBarrier(2);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var first = workers.submit(() -> {
                start.await(5, TimeUnit.SECONDS);
                return cartService.addItemToCart(userId, new AddCartItemRequest(variantId, 1));
            });
            var second = workers.submit(() -> {
                start.await(5, TimeUnit.SECONDS);
                return cartService.addItemToCart(userId, new AddCartItemRequest(variantId, 2));
            });
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }
        var cart = cartService.getCart(userId);
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().getFirst().getQuantity());
        assertEquals(1, jdbc.queryForObject("select count(*) from carts where user_id = ?", Integer.class, userId));
    }

    @Test
    void checkoutCleanupParticipatesInTheCallersRollback() {
        var added = cartService.addItemToCart(userId, new AddCartItemRequest(variantId, 2));
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var snapshot = cartService.lockAndGetCheckoutCart(userId);
            cartService.clearPurchasedItems(userId, List.of(snapshot.getItems().getFirst().getCartItemId()));
            throw new IllegalStateException("Simulated order failure");
        }));
        assertEquals(added.getItems(), cartService.getCart(userId).getItems());
    }

    @Test
    void checkoutHelpersRejectCallsWithoutATransaction() {
        assertThrows(IllegalTransactionStateException.class, () -> cartService.lockAndGetCheckoutCart(userId));
        assertThrows(IllegalTransactionStateException.class, () -> cartService.clearPurchasedItems(userId, List.of()));
    }

    @Test
    void concurrentAddWaitsForCheckoutAndSurvivesPurchasedItemCleanup() throws Exception {
        cartService.addItemToCart(userId, new AddCartItemRequest(variantId, 2));
        var locked = new CountDownLatch(1);
        var releaseCheckout = new CountDownLatch(1);
        var adding = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var checkout = workers.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                var snapshot = cartService.lockAndGetCheckoutCart(userId);
                locked.countDown();
                try {
                    if (!releaseCheckout.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Checkout release timed out");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                cartService.clearPurchasedItems(userId, snapshot.getItems().stream().map(item -> item.getCartItemId()).toList());
            }));
            try {
                assertTrue(locked.await(5, TimeUnit.SECONDS));
                var add = workers.submit(() -> {
                    adding.countDown();
                    return cartService.addItemToCart(userId, new AddCartItemRequest(otherVariantId, 1));
                });
                assertTrue(adding.await(5, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class, () -> add.get(200, TimeUnit.MILLISECONDS));
                releaseCheckout.countDown();
                checkout.get(10, TimeUnit.SECONDS);
                add.get(10, TimeUnit.SECONDS);
            } finally {
                releaseCheckout.countDown();
            }
        }
        var remaining = cartService.getCart(userId);
        assertEquals(1, remaining.getItems().size());
        assertEquals(otherVariantId, remaining.getItems().getFirst().getVariantId());
    }

    private Long createVariant(Long productId, String sku) {
        return jdbc.queryForObject("""
                insert into product_variants (product_id, sku, ram, storage, color, price, quantity)
                values (?, ?, '16GB', '512GB', 'Silver', 100.50, 20) returning id
                """, Long.class, productId, sku);
    }
}
