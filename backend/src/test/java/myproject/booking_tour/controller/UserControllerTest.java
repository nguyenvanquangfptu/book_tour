package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.ChangePasswordRequest;
import myproject.booking_tour.dto.request.UpdateProfileRequest;
import myproject.booking_tour.dto.response.UserResponse;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.security.CustomUserDetails;
import myproject.booking_tour.service.UserService;
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

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

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
    private UserService userService;

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
    void getAllUsers_ShouldReturn200() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(new UserResponse()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMyProfile_ShouldReturn200() throws Exception {
        Mockito.when(userService.getMyProfile(10L)).thenReturn(new UserResponse());

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateMyProfile_ShouldReturn200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@test.com");

        Mockito.when(userService.updateProfile(eq(10L), any(UpdateProfileRequest.class)))
                .thenReturn(new UserResponse());

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changeMyPassword_ShouldReturn200() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldpass");
        request.setNewPassword("newpass");
        request.setConfirmPassword("newpass");

        mockMvc.perform(put("/api/users/profile/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

