package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.BookingRequest;
import myproject.booking_tour.dto.response.BookingResponse;
import myproject.booking_tour.dto.response.PageResponse;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.security.CustomUserDetails;
import myproject.booking_tour.service.BookingService;
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

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtService jwtService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

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
    void createBooking_ShouldReturn200() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setTourId(1L);
        request.setNumberOfPeople(2);
        request.setTravelDate(java.time.LocalDate.now());

        Mockito.when(bookingService.createBooking(any(BookingRequest.class), eq(10L)))
                .thenReturn(new BookingResponse());

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMyBookings_ShouldReturn200() throws Exception {
        Mockito.when(bookingService.getMyBookings(10L)).thenReturn(List.of(new BookingResponse()));

        mockMvc.perform(get("/api/bookings/my-bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAllBookings_ShouldReturn200() throws Exception {
        Mockito.when(bookingService.getAllBookings(0, 10)).thenReturn(new PageResponse<>());

        mockMvc.perform(get("/api/bookings?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

