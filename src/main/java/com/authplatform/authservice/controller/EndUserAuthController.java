package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.*;
import com.authplatform.authservice.service.EndUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/p/{apiKey}/auth")
@RequiredArgsConstructor
public class EndUserAuthController {

    private final EndUserService endUserService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerEndUser(
            @PathVariable String apiKey,
            @Valid @RequestBody EndUserRegisterRequest request
    ) {
        endUserService.register(apiKey, request);
        return new ResponseEntity<>(new ApiResponse(true, "User registered successfully."), HttpStatus.CREATED);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(
            @PathVariable String apiKey,
            @RequestParam("token") String token
    ) {
        endUserService.verifyEmail(apiKey, token);
        return ResponseEntity.ok(new ApiResponse(true, "Email verified successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginEndUser(@PathVariable String apiKey, @Valid @RequestBody EndUserLoginRequest request) {
        AuthResponse authResponse = endUserService.login(apiKey, request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @PathVariable String apiKey,
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        endUserService.forgotPassword(apiKey, request);
        return ResponseEntity.ok(new ApiResponse(true, "OTP has been sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @PathVariable String apiKey,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        endUserService.resetPassword(apiKey, request);
        return ResponseEntity.ok(new ApiResponse(true, "Password has been reset successfully."));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@PathVariable String apiKey, @Valid @RequestBody TokenValidationRequest request) {
        TokenValidationResponse response = endUserService.validateToken(apiKey, request.getToken());
        if (!response.isValid()) {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(response);
    }
}
