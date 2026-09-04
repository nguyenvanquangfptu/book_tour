package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import myproject.booking_tour.exception.WebhookVerificationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayOSWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class PayOSWebhookControllerTest {

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
    private PaymentService paymentService;

    @MockBean
    private PayOS payOS;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void handlePayOSWebhook_ShouldReturn200() throws Exception {
        String jsonPayload = "{\"code\":\"00\",\"desc\":\"success\",\"data\":{}}";

        mockMvc.perform(post("/api/payment/payos_transfer_handler")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"));
    }

    /**
     * Loi VINH VIEN (chu ky sai / payload gia mao): phai tra 2xx de PayOS NGUNG retry.
     * Neu tra 4xx-5xx, PayOS se gui lai theo backoff hang gio ma khong bao gio thanh cong.
     */
    @Test
    void handlePayOSWebhook_KhiChuKyKhongHopLe_TraVe200DeNgungRetry() throws Exception {
        doThrow(new WebhookVerificationException("Chu ky webhook PayOS khong hop le"))
                .when(paymentService).processPayOSWebhook(any());

        String jsonPayload = "{\"code\":\"00\",\"desc\":\"success\",\"data\":{}}";

        mockMvc.perform(post("/api/payment/payos_transfer_handler")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("false"));
    }

    /**
     * Loi TAM THOI (mat ket noi DB, PayOS API timeout): phai tra 5xx de PayOS retry.
     * Truoc day moi loi deu tra 400 -> PayOS coi la loi phia client va co the bo luon,
     * dan den user da thanh toan nhung booking ket o trang thai PENDING.
     */
    @Test
    void handlePayOSWebhook_KhiLoiHeThong_TraVe503DePayOSRetry() throws Exception {
        doThrow(new RuntimeException("Mat ket noi database"))
                .when(paymentService).processPayOSWebhook(any());

        String jsonPayload = "{\"code\":\"00\",\"desc\":\"success\",\"data\":{}}";

        mockMvc.perform(post("/api/payment/payos_transfer_handler")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isServiceUnavailable());
    }
}

