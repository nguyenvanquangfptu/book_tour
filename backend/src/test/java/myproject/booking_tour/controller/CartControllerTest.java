package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.CartItemRequest;
import myproject.booking_tour.dto.response.CartResponse;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.security.CustomUserDetails;
import myproject.booking_tour.service.CartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    // JwtAuthenticationFilter nay inject repository nay; @WebMvcTest khong nap
    // tang repository nen phai mock.
    @org.springframework.boot.test.mock.mockito.MockBean
    private myproject.booking_tour.repository.InvalidatedTokenRepository invalidatedTokenRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtService jwtService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private SecurityContext mockSecurityContext;
    private Authentication mockAuthentication;
    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        mockSecurityContext = Mockito.mock(SecurityContext.class);
        mockAuthentication = Mockito.mock(Authentication.class);
        mockUserDetails = Mockito.mock(CustomUserDetails.class);
        User user = new User();
        user.setId(10L);

        Mockito.when(mockUserDetails.getUser()).thenReturn(user);
        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);
        Mockito.when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    void getCart_ShouldReturn200() throws Exception {
        Mockito.when(cartService.getCartForUser(10L)).thenReturn(new CartResponse());

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void addToCart_ShouldReturn200() throws Exception {
        CartItemRequest request = new CartItemRequest();
        String requestBody = "{\"tourId\":1,\"guests\":2,\"startDate\":\"2024-12-01\"}";

        Mockito.when(cartService.addToCart(eq(10L), any(CartItemRequest.class))).thenReturn(new CartResponse());

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void removeFromCart_ShouldReturn200() throws Exception {
        Mockito.when(cartService.removeFromCart(10L, 1L)).thenReturn(new CartResponse());

        mockMvc.perform(delete("/api/cart/remove/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

