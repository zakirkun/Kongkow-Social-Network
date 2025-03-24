package app.kongkow.social.auth.service;

import app.kongkow.social.auth.dto.AuthResponse;
import app.kongkow.social.auth.dto.LoginRequest;
import app.kongkow.social.auth.dto.SignupRequest;
import app.kongkow.social.auth.security.JwtTokenProvider;
import app.kongkow.common.exception.AuthenticationException;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        // Find user information
        User user = userRepository.findByUsername(loginRequest.getUsernameOrEmail())
                .orElse(userRepository.findByEmail(loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> new AuthenticationException("User not found with username or email: " + loginRequest.getUsernameOrEmail())));

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse register(SignupRequest signupRequest) {
        // Check if username is taken
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }

        // Check if email is taken
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        // Create new user
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .fullName(signupRequest.getFullName())
                .bio(signupRequest.getBio())
                .roles(roles)
                .accountEnabled(true)
                .accountLocked(false)
                .build();

        User savedUser = userRepository.save(user);

        // Generate token for the registered user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signupRequest.getUsername(),
                        signupRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .build();
    }
}