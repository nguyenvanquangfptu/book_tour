package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;
import myproject.booking_tour.security.CustomUserDetails;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.PaymentResponse;
import myproject.booking_tour.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtService jwtService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUpAuthenticatedUser() {
        // addFilters = false skips JwtAuthenticationFilter, so the SecurityContext must be
        // populated manually to exercise the controller's ownership checks.
        Role role = new Role();
        role.setName("CUSTOMER");
        User user = new User();
        user.setId(1L);
        user.setRole(role);
        CustomUserDetails principal = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPayment_ShouldReturn200() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);

        Mockito.when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(new PaymentResponse());

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getPaymentById_ShouldReturn200() throws Exception {
        Mockito.when(paymentService.getPaymentById(eq(1L), anyLong(), anyBoolean())).thenReturn(new PaymentResponse());

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createPayOSUrl_ShouldReturn200() throws Exception {
        Mockito.when(paymentService.createPaymentUrl(eq(1L), anyLong(), anyBoolean())).thenReturn("http://payos.url");

        mockMvc.perform(get("/api/payments/create-payos-url")
                .param("bookingId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createPayOSUrl_ShouldPassAuthenticatedUser_NotJustTheBookingId() throws Exception {
        // Danh tính người gọi phải đi kèm xuống service. Trước đây endpoint chỉ
        // truyền bookingId, nên bất kỳ tài khoản CUSTOMER nào cũng tạo được link
        // thanh toán cho đơn của người khác.
        Mockito.when(paymentService.createPaymentUrl(eq(1L), anyLong(), anyBoolean())).thenReturn("http://payos.url");

        mockMvc.perform(get("/api/payments/create-payos-url")
                .param("bookingId", "1"))
                .andExpect(status().isOk());

        Mockito.verify(paymentService).createPaymentUrl(1L, 1L, false);
    }
}

