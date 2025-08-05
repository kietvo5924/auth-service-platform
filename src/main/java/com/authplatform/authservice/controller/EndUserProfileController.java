package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.ApiResponse;
import com.authplatform.authservice.dto.ChangePasswordRequest;
import com.authplatform.authservice.dto.EndUserResponse;
import com.authplatform.authservice.dto.UpdateMyProfileRequest;
import com.authplatform.authservice.service.EndUserService;
import com.authplatform.authservice.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eu/me")
@RequiredArgsConstructor
public class EndUserProfileController {

    private final EndUserService endUserService;
    private final JwtService jwtService;

    private String getApiKeyFromToken(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        return jwtService.extractClaim(token, claims -> claims.get("apiKey", String.class));
    }

    @GetMapping
    public ResponseEntity<EndUserResponse> getMyProfile(Authentication authentication) {
        String apiKey = getApiKeyFromToken(authentication); // Lấy apiKey từ token
        EndUserResponse profile = endUserService.getMyProfile(authentication.getName(), apiKey);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<EndUserResponse> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequest request,
            Authentication authentication
    ) {
        String apiKey = getApiKeyFromToken(authentication);
        EndUserResponse updatedProfile = endUserService.updateMyProfile(authentication.getName(), apiKey, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        String apiKey = getApiKeyFromToken(authentication);
        endUserService.changeMyPassword(authentication.getName(), apiKey, request);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully."));
    }
}
