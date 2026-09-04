package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.AccommodationRequest;
import myproject.booking_tour.dto.response.AccommodationResponse;
import myproject.booking_tour.service.AccommodationService;
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

@WebMvcTest(AccommodationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccommodationControllerTest {

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
    private AccommodationService accommodationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllAccommodations_ShouldReturn200() throws Exception {
        Mockito.when(accommodationService.getAllAccommodations()).thenReturn(List.of(new AccommodationResponse()));

        mockMvc.perform(get("/api/accommodations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Accommodations retrieved successfully!"));
    }

    @Test
    void getAccommodationById_ShouldReturn200() throws Exception {
        Mockito.when(accommodationService.getAccommodationById(1L)).thenReturn(new AccommodationResponse());

        mockMvc.perform(get("/api/accommodations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createAccommodation_ShouldReturn200() throws Exception {
        AccommodationRequest request = new AccommodationRequest();
        request.setName("Test Hotel");
        request.setType("Hotel");
        request.setAddress("123 Test St");

        Mockito.when(accommodationService.createAccommodation(any(AccommodationRequest.class)))
                .thenReturn(new AccommodationResponse());

        mockMvc.perform(post("/api/accommodations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateAccommodation_ShouldReturn200() throws Exception {
        AccommodationRequest request = new AccommodationRequest();
        request.setName("Updated Hotel");
        request.setAddress("123 Test St");

        Mockito.when(accommodationService.updateAccommodation(eq(1L), any(AccommodationRequest.class)))
                .thenReturn(new AccommodationResponse());

        mockMvc.perform(put("/api/accommodations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteAccommodation_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/accommodations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

