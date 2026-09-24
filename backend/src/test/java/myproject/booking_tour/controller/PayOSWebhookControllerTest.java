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

    private void authenticateCustomer(long userId) {
        // addFilters = false bỏ qua JwtAuthenticationFilter nên SecurityContext
        // phải tự dựng để chạy được chốt kiểm tra quyền trong controller.
        myproject.booking_tour.entity.Role role = new myproject.booking_tour.entity.Role();
        role.setName("CUSTOMER");
        myproject.booking_tour.entity.User user = new myproject.booking_tour.entity.User();
        user.setId(userId);
        user.setRole(role);
        myproject.booking_tour.security.CustomUserDetails principal =
                new myproject.booking_tour.security.CustomUserDetails(user);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }

    @org.junit.jupiter.api.AfterEach
    void clearContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void verify_ShouldRefuse_WhenOrderCodeIsNotInOurDatabase() throws Exception {
        // Điều kiện cũ là "ownerId != null && !isAdmin && ..." nên orderCode lạ
        // đi THẲNG qua chốt kiểm tra quyền, và code vẫn hỏi PayOS - biến endpoint
        // thành công cụ dò trạng thái đơn hàng PayOS bất kỳ.
        authenticateCustomer(1L);
        Mockito.when(paymentService.getPaymentOwnerUserIdByOrderCode("999")).thenReturn(null);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/payment/payos_transfer_handler/verify")
                        .param("orderCode", "999"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(payOS);
    }

    @Test
    void verify_ShouldRefuse_WhenOrderCodeBelongsToSomeoneElse() throws Exception {
        authenticateCustomer(1L);
        Mockito.when(paymentService.getPaymentOwnerUserIdByOrderCode("777")).thenReturn(7L);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/payment/payos_transfer_handler/verify")
                        .param("orderCode", "777"))
                .andExpect(status().isBadRequest())
                // Thông báo của chốt kiểm tra quyền phải đến nơi nguyên vẹn.
                // Khối catch(Exception) cũ nuốt chính ngoại lệ này rồi bọc lại
                // thành "Lỗi xác minh thanh toán: Bạn không có quyền...".
                .andExpect(jsonPath("$.message").value("Bạn không có quyền xác minh đơn hàng này!"));

        Mockito.verifyNoInteractions(payOS);
    }

    @Test
    void verify_ShouldReturn400_WhenOrderCodeIsNotANumber() throws Exception {
        authenticateCustomer(1L);
        Mockito.when(paymentService.getPaymentOwnerUserIdByOrderCode("abc")).thenReturn(1L);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/payment/payos_transfer_handler/verify")
                        .param("orderCode", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mã đơn hàng không hợp lệ."));

        Mockito.verifyNoInteractions(payOS);
    }

    @Test
    void verify_ShouldReturn500AndLeakNothing_WhenPayOSCallFails() throws Exception {
        // Sự cố phía PayOS là lỗi máy chủ, không phải lỗi của người gọi. Khối
        // catch cũ biến nó thành 400 kèm nguyên văn thông báo của tầng dưới.
        authenticateCustomer(1L);
        Mockito.when(paymentService.getPaymentOwnerUserIdByOrderCode("555")).thenReturn(1L);
        Mockito.when(payOS.paymentRequests())
                .thenThrow(new RuntimeException("connection refused to api-merchant.payos.vn"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/payment/payos_transfer_handler/verify")
                        .param("orderCode", "555"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Đã có lỗi xảy ra, vui lòng thử lại sau!"));
    }
}

