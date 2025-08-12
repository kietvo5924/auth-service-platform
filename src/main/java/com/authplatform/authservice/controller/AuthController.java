package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.*;
import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.service.JwtService;
import com.authplatform.authservice.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OwnerService ownerService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.frontend.verify-email-url}")
    private String verifyEmailResultUrl;

    // --- API ĐĂNG KÝ ---
    @PostMapping("/register")
    public ResponseEntity<OwnerResponse> registerOwner(@Valid @RequestBody RegisterOwnerRequest registerRequest) {
        OwnerResponse registeredOwner = ownerService.registerOwner(registerRequest);
        return new ResponseEntity<>(registeredOwner, HttpStatus.CREATED);
    }

    // --- API XÁC THỰC EMAIL ---
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam("token") String token) {
        try {
            ownerService.verifyEmail(token);
            return ResponseEntity.ok(new ApiResponse(true, "Email verified successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    // --- API ĐĂNG NHẬP ---
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        Owner ownerDetails = (Owner) authentication.getPrincipal();
        String token = jwtService.generateOwnerLoginToken(ownerDetails);
        return ResponseEntity.ok(AuthResponse.builder().accessToken(token).build());
    }

    // --- API QUÊN MẬT KHẨU ---
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        ownerService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "OTP has been sent to your email."));
    }

    // --- API ĐẶT LẠI MẬT KHẨU ---
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ownerService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse(true, "Password has been reset successfully."));
    }
}
