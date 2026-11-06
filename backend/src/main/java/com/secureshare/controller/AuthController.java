package com.secureshare.controller;

import com.secureshare.dto.*;
import com.secureshare.entity.User;
import com.secureshare.repository.UserRepository;
import com.secureshare.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req,
                                                HttpServletRequest http) {
        return ResponseEntity.ok(authService.login(req, http.getRemoteAddr(),
                http.getHeader("User-Agent")));
    }

    // Endpoint to get any user's PUBLIC key (needed for key re-encryption on share)
    // Public keys are safe to expose — they are meant to be shared
    @GetMapping("/public-key/{username}")
    public ResponseEntity<Map<String, String>> getPublicKey(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return ResponseEntity.ok(Map.of(
                "username", username,
                "publicKey", user.getPublicKey()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("SecureShare API running");
    }
}
