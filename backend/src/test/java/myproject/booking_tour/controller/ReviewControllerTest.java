package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.ReviewRequest;
import myproject.booking_tour.dto.response.ReviewResponse;
import myproject.booking_tour.service.ReviewService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtService jwtService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private SecurityContext mockSecurityContext;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        mockSecurityContext = Mockito.mock(SecurityContext.class);
        mockAuthentication = Mockito.mock(Authentication.class);
        
        Mockito.when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        Mockito.when(mockAuthentication.getName()).thenReturn("testuser");

        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    void addReview_ShouldReturn200() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setTourId(1L);
        request.setRating(5);
        request.setComment("Great!");

        Mockito.when(reviewService.addReview(any(ReviewRequest.class), eq("testuser")))
                .thenReturn(new ReviewResponse());

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateReview_ShouldReturn200() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setRating(4);
        request.setTourId(1L);

        Mockito.when(reviewService.updateReview(eq(1L), any(ReviewRequest.class), eq("testuser")))
                .thenReturn(new ReviewResponse());

        mockMvc.perform(put("/api/reviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getReviewsByTour_ShouldReturn200() throws Exception {
        Mockito.when(reviewService.getReviewsByTourId(1L)).thenReturn(List.of(new ReviewResponse()));

        mockMvc.perform(get("/api/reviews/tour/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteReview_ShouldReturn200() throws Exception {
        // deleteReview method uses Authentication argument instead of SecurityContextHolder
        // Since we bypass filters, it might be null unless we inject it, 
        // but with MockMvc we can use Principal or configure it.
        // The endpoint is: deleteReview(@PathVariable Long id, Authentication authentication)
        // With @AutoConfigureMockMvc(addFilters=false), Authentication is not injected by Spring Security and becomes null.
        // We will just do a basic test and mock the principal.
        
        mockMvc.perform(delete("/api/reviews/1")
                .principal(() -> "testuser"))
                .andExpect(status().isOk());
    }
}

