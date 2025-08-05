package com.authplatform.authservice.service;

import com.authplatform.authservice.dto.OwnerResponse;
import com.authplatform.authservice.dto.RegisterOwnerRequest;
import com.authplatform.authservice.dto.ResetPasswordRequest;
import com.authplatform.authservice.dto.UpdateOwnerRequest;
import com.authplatform.authservice.exception.EmailAlreadyExistsException;
import com.authplatform.authservice.exception.InvalidTokenException;
import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.model.Role;
import com.authplatform.authservice.repository.OwnerRepository;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    // --- LOGIC ĐĂNG KÝ ---
    @Transactional
    public OwnerResponse registerOwner(RegisterOwnerRequest request) {
        if (ownerRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Error: Email is already in use!");
        }

        Owner newOwner = Owner.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .emailVerified(false)
                .build();

        Owner savedOwner = ownerRepository.save(newOwner);

        // Tạo token xác thực email
        String verificationToken = jwtService.generateEmailVerificationToken(savedOwner.getEmail());

        // Gửi email với link trỏ về backend của chúng ta
        String verificationLink = "http://localhost:8080/api/platform/auth/verify-email?token=" + verificationToken;
        String emailBody = "<h1>Cảm ơn bạn đã đăng ký!</h1>"
                + "<p>Vui lòng nhấp vào link sau để xác thực tài khoản của bạn (link có hiệu lực trong 15 phút):</p>"
                + "<a href=\"" + verificationLink + "\">Xác thực tài khoản</a>";

        emailService.sendHtmlEmail(savedOwner.getEmail(), "Xác thực tài khoản", emailBody);

        // Trả về thông tin cơ bản, không có token đăng nhập
        return mapToOwnerResponse(savedOwner);
    }

    // --- LOGIC XÁC THỰC EMAIL ---
    @Transactional
    public void verifyEmail(String token) {
        String email = jwtService.extractUsername(token);

        Owner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found for this token."));

        if (!jwtService.isVerificationTokenValid(token, owner)) {
            throw new InvalidTokenException("Token is invalid or expired.");
        }

        owner.setEmailVerified(true);
        ownerRepository.save(owner);
    }

    // Tạo một cache để lưu OTP
    private final Cache<String, String> otpCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    // Hàm khởi tảo mã OTP 6 số ngẫu nhiên
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    // Xử lý yêu cầu quên mật khẩu
    public void forgotPassword(String email) {
        Owner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found with email: " + email));

        if (!owner.isEmailVerified()) {
            throw new IllegalStateException("Email has not been verified yet.");
        }

        String otp = generateOtp();
        otpCache.put(email, otp);

        String emailBody = "<h1>Yêu cầu đặt lại mật khẩu</h1>"
                        + "<p>Mã OTP của bạn là: <b>" + otp + "</b></p>"
                        + "<p>Mã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>";
        emailService.sendHtmlEmail(email, "Mã OTP đặt lại mật khẩu", emailBody);
    }

    // Xử lý đặt lại mật khẩu mới
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String storedOtp = otpCache.getIfPresent(request.getEmail());

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new InvalidTokenException("Invalid or expired OTP.");
        }

        Owner owner = ownerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        owner.setPassword(passwordEncoder.encode(request.getNewPassword()));
        owner.setPasswordLastChangedAt(Instant.now());
        ownerRepository.save(owner);

        otpCache.invalidate(request.getEmail());
    }

    // Lấy tất cả Owner
    public List<OwnerResponse> getAllOwners() {
        return ownerRepository.findAll().stream()
                .map(this::mapToOwnerResponse)
                .collect(Collectors.toList());
    }

    // Owner tự cập nhật thông tin
    @Transactional
    public OwnerResponse updateOwner(String ownerEmail, UpdateOwnerRequest request) {
        Owner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found"));

        owner.setFullName(request.getFullName());
        Owner updatedOwner = ownerRepository.save(owner);

        return mapToOwnerResponse(updatedOwner);
    }

    // Admin khóa/mở khóa Owner
    @Transactional
    public void toggleOwnerLock(Long ownerId, boolean shouldBeLocked) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found"));

        owner.setLocked(shouldBeLocked);
        ownerRepository.save(owner);
    }

    // Hàm chuyển đổi Entity sang DTO
    private OwnerResponse mapToOwnerResponse(Owner owner) {
        OwnerResponse response = new OwnerResponse();
        response.setId(owner.getId());
        response.setFullName(owner.getFullName());
        response.setEmail(owner.getEmail());
        response.setRole(owner.getRole());
        response.setEmailVerified(owner.isEmailVerified());
        response.setLocked(owner.isLocked());
        return response;
    }
}
