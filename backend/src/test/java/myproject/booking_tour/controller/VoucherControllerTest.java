package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.VoucherRequest;
import myproject.booking_tour.dto.response.VoucherResponse;
import myproject.booking_tour.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherController.class)
@AutoConfigureMockMvc(addFilters = false)
class VoucherControllerTest {

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
    private VoucherService voucherService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllVouchers_ShouldReturn200() throws Exception {
        Mockito.when(voucherService.getAllVouchers()).thenReturn(List.of(new VoucherResponse()));

        mockMvc.perform(get("/api/vouchers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getVoucherById_ShouldReturn200() throws Exception {
        Mockito.when(voucherService.getVoucherById(1L)).thenReturn(new VoucherResponse());

        mockMvc.perform(get("/api/vouchers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getVoucherByCode_ShouldReturn200() throws Exception {
        Mockito.when(voucherService.getVoucherByCode("DISCOUNT")).thenReturn(new VoucherResponse());

        mockMvc.perform(get("/api/vouchers/code/DISCOUNT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createVoucher_ShouldReturn200() throws Exception {
        VoucherRequest request = new VoucherRequest();
        request.setCode("DISCOUNT");
        request.setValidFrom(java.time.LocalDateTime.now());
        request.setValidUntil(java.time.LocalDateTime.now().plusDays(1));

        Mockito.when(voucherService.createVoucher(any(VoucherRequest.class)))
                .thenReturn(new VoucherResponse());

        mockMvc.perform(post("/api/vouchers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateVoucher_ShouldReturn200() throws Exception {
        VoucherRequest request = new VoucherRequest();
        request.setCode("NEWDISCOUNT");
        request.setValidFrom(java.time.LocalDateTime.now());
        request.setValidUntil(java.time.LocalDateTime.now().plusDays(1));

        Mockito.when(voucherService.updateVoucher(eq(1L), any(VoucherRequest.class)))
                .thenReturn(new VoucherResponse());

        mockMvc.perform(put("/api/vouchers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteVoucher_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/vouchers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

