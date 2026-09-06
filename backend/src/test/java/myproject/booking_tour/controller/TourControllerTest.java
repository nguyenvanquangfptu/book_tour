package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.TourRequest;
import myproject.booking_tour.dto.response.TourResponse;
import myproject.booking_tour.dto.response.PageResponse;
import myproject.booking_tour.dto.response.PopularDestinationResponse;
import myproject.booking_tour.service.TourService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TourController.class)
@AutoConfigureMockMvc(addFilters = false)
class TourControllerTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtService jwtService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TourService tourService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPopularDestinations_ShouldReturn200() throws Exception {
        Mockito.when(tourService.getPopularDestinations(4)).thenReturn(List.of(new PopularDestinationResponse()));

        mockMvc.perform(get("/api/tours/popular-destinations?limit=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getTourById_ShouldReturn200() throws Exception {
        Mockito.when(tourService.getTourById(1L)).thenReturn(new TourResponse());

        mockMvc.perform(get("/api/tours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void searchAndFilterTours_ShouldReturn200() throws Exception {
        PageResponse<TourResponse> pageResponse = new PageResponse<>();
        Mockito.when(tourService.searchAndFilterTours(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/tours/search?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createTour_ShouldReturn200() throws Exception {
        TourRequest request = new TourRequest();
        request.setTitle("New Tour");
        request.setPrice(new java.math.BigDecimal("100.00"));

        Mockito.when(tourService.createTour(any(TourRequest.class))).thenReturn(new TourResponse());

        mockMvc.perform(post("/api/tours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changeTourStatus_ShouldReturn200() throws Exception {
        Mockito.when(tourService.changeStatus(1L, "INACTIVE")).thenReturn(new TourResponse());

        mockMvc.perform(put("/api/tours/1/status")
                .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

