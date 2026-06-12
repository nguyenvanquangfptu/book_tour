package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.response.UserResponse;
import myproject.booking_tour.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        return user;
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        if (user.getRole() != null) {
            response.setRole(user.getRole().getName());
        }
        return response;
    }
}
