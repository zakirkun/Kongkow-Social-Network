package app.kongkow.social.auth.controller;

import app.kongkow.social.auth.dto.AuthResponse;
import app.kongkow.social.auth.dto.LoginRequest;
import app.kongkow.social.auth.dto.SignupRequest;
import app.kongkow.social.auth.service.AuthService;
import app.kongkow.common.payload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignupRequest signupRequest) {
        AuthResponse response = authService.register(signupRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }
}