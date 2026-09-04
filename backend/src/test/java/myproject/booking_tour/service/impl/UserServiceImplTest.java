package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.ChangePasswordRequest;
import myproject.booking_tour.dto.request.UpdateProfileRequest;
import myproject.booking_tour.dto.response.UserResponse;
import myproject.booking_tour.entity.AuditLog;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.UserMapper;
import myproject.booking_tour.repository.AuditLogRepository;
import myproject.booking_tour.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@gmail.com");
        mockUser.setFullName("Test User");
        mockUser.setPhone("0123456789");
        mockUser.setPassword("encodedOldPassword");

        mockUserResponse = new UserResponse();
        mockUserResponse.setId(1L);
        mockUserResponse.setEmail("test@gmail.com");
        mockUserResponse.setFullName("Test User");
        mockUserResponse.setPhone("0123456789");
    }

    // ==========================================
    // GET USER BY ID & GET MY PROFILE
    // ==========================================
    @Test
    void getUserById_ShouldReturnUserResponse_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@gmail.com", result.getEmail());
        
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void getMyProfile_ShouldReturnUserResponse_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

        UserResponse result = userService.getMyProfile(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    // ==========================================
    // GET ALL USERS
    // ==========================================
    @Test
    void getAllUsers_ShouldReturnListOfUserResponses() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@gmail.com", result.get(0).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    // ==========================================
    // UPDATE PROFILE
    // ==========================================
    @Test
    void updateProfile_ShouldUpdateAndReturnResponse_WhenValidRequest() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setEmail("test@gmail.com"); // Trùng với email hiện tại nên hợp lệ
        request.setPhone("9876543210");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(mockUserResponse);

        UserResponse result = userService.updateProfile(1L, request);

        assertNotNull(result);
        assertEquals("Updated Name", mockUser.getFullName());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateProfile_ShouldThrowBadRequestException_WhenEmailIsTakenByAnother() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("taken@gmail.com"); // Email mới

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByEmailIgnoreCase("taken@gmail.com")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, 
                () -> userService.updateProfile(1L, request));
        assertTrue(ex.getMessage().contains("Email is already registered"));
        
        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // CHANGE PASSWORD
    // ==========================================
    @Test
    void changePassword_ShouldChangeSuccessfully_WhenValidRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");
        request.setConfirmPassword("newPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPassword");

        userService.changePassword(1L, request);

        assertEquals("encodedNewPassword", mockUser.getPassword());
        verify(userRepository, times(1)).save(mockUser);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void changePassword_ShouldThrowBadRequestException_WhenOldPasswordIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongOldPass", "encodedOldPassword")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, 
                () -> userService.changePassword(1L, request));
        assertTrue(ex.getMessage().contains("Incorrect old password"));
                
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_ShouldThrowBadRequestException_WhenNewPasswordDoesNotMatchConfirm() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");
        request.setConfirmPassword("differentPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, 
                () -> userService.changePassword(1L, request));
        assertTrue(ex.getMessage().contains("do not match"));
                
        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // DELETE USER
    // ==========================================
    @Test
    void deleteUser_ShouldDelete_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(mockUser);
    }
    
    @Test
    void deleteUser_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).delete(any());
    }
}
