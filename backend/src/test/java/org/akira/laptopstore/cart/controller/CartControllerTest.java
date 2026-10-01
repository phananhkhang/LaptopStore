package org.akira.laptopstore.cart.controller;

import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.service.CartService;
import org.akira.laptopstore.security.CustomAccessDeniedHandler;
import org.akira.laptopstore.security.JwtAuthenticationEntryPoint;
import org.akira.laptopstore.security.SecurityConfig;
import org.akira.laptopstore.security.filter.JwtAuthenticationFilter;
import org.akira.laptopstore.security.jwt.JwtTokenProvider;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.akira.laptopstore.security.principal.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(CartController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class CartControllerTest {

    private static final String CART_PATH = "/api/v1/me/cart";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @ParameterizedTest
    @ValueSource(longs = {42L, 84L})
    void cartOperationsUseTheAuthenticatedUser(Long userId) throws Exception {
        CustomUserDetails principal = new CustomUserDetails(userId, "user" + userId + "@example.com",
                "unused", 0, "ACTIVE", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(cartService.getCart(userId)).thenReturn(new CartResponse(10L, List.of(), 0, BigDecimal.ZERO));

        mockMvc.perform(get(CART_PATH).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
        mockMvc.perform(post(CART_PATH + "/items").with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productVariantId\":7,\"quantity\":2}"))
                .andExpect(status().isCreated());
        mockMvc.perform(patch(CART_PATH + "/items/9").with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete(CART_PATH + "/items/9").with(user(principal)))
                .andExpect(status().isNoContent());

        verify(cartService).getCart(userId);
        verify(cartService).addItemToCart(userId, new AddCartItemRequest(7L, 2));
        verify(cartService).updateCartItem(userId, 9L, new UpdateCartItemRequest(3));
        verify(cartService).removeCartItem(userId, 9L);
    }

    @Test
    void cartOperationsRequireAuthentication() throws Exception {
        mockMvc.perform(get(CART_PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        mockMvc.perform(post(CART_PATH + "/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productVariantId\":7,\"quantity\":2}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch(CART_PATH + "/items/9").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete(CART_PATH + "/items/9"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cartService);
    }
}
