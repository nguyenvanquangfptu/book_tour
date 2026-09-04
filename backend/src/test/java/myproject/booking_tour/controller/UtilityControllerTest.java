package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.UtilityRequest;
import myproject.booking_tour.dto.response.UtilityResponse;
import myproject.booking_tour.service.UtilityService;
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

@WebMvcTest(UtilityController.class)
@AutoConfigureMockMvc(addFilters = false)
class UtilityControllerTest {

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
    private UtilityService utilityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllUtilities_ShouldReturn200() throws Exception {
        Mockito.when(utilityService.getAllUtilities()).thenReturn(List.of(new UtilityResponse()));

        mockMvc.perform(get("/api/utilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUtilityById_ShouldReturn200() throws Exception {
        Mockito.when(utilityService.getUtilityById(1L)).thenReturn(new UtilityResponse());

        mockMvc.perform(get("/api/utilities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createUtility_ShouldReturn200() throws Exception {
        UtilityRequest request = new UtilityRequest();
        request.setName("Wifi");

        Mockito.when(utilityService.createUtility(any(UtilityRequest.class)))
                .thenReturn(new UtilityResponse());

        mockMvc.perform(post("/api/utilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateUtility_ShouldReturn200() throws Exception {
        UtilityRequest request = new UtilityRequest();
        request.setName("Wifi updated");

        Mockito.when(utilityService.updateUtility(eq(1L), any(UtilityRequest.class)))
                .thenReturn(new UtilityResponse());

        mockMvc.perform(put("/api/utilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteUtility_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/utilities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

