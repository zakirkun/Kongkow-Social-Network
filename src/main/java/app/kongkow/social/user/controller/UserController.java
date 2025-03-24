package app.kongkow.social.user.controller;

import app.kongkow.common.payload.ApiResponse;
import app.kongkow.social.user.dto.UserDto;
import app.kongkow.social.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto.UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto.UserResponse user = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto.UserResponse> getUserById(@PathVariable Long id) {
        UserDto.UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto.UserResponse> getUserByUsername(@PathVariable String username) {
        UserDto.UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto.UserResponse>> getAllUsers() {
        List<UserDto.UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto.UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDto.UserProfileUpdateRequest request) {
        UserDto.UserResponse user = userService.getUserByUsername(userDetails.getUsername());
        UserDto.UserResponse updatedUser = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDto.PasswordChangeRequest request) {
        UserDto.UserResponse user = userService.getUserByUsername(userDetails.getUsername());
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }
}