package com.authplatform.authservice.service;

import com.authplatform.authservice.dto.*;
import com.authplatform.authservice.exception.EmailAlreadyExistsException;
import com.authplatform.authservice.exception.InvalidTokenException;
import com.authplatform.authservice.exception.ProjectNotFoundException;
import com.authplatform.authservice.model.EndUser;
import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.model.Project;
import com.authplatform.authservice.model.ProjectRole;
import com.authplatform.authservice.repository.EndUserRepository;
import com.authplatform.authservice.repository.OwnerRepository;
import com.authplatform.authservice.repository.ProjectRepository;
import com.authplatform.authservice.repository.ProjectRoleRepository;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EndUserService {

    private final EndUserRepository endUserRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final OwnerRepository ownerRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EndUser register(String apiKey, EndUserRegisterRequest request) {
        Project project = projectRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found for the given API key."));

        if (endUserRepository.existsByEmailAndProject(request.getEmail(), project)) {
            throw new EmailAlreadyExistsException("Email is already in use for this project.");
        }

        ProjectRole defaultRole = projectRoleRepository.findByNameAndProject("USER", project)
                .orElseThrow(() -> new IllegalStateException("Default USER role not found for this project."));

        EndUser newUser = EndUser.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .project(project)
                .roles(Set.of(defaultRole))
                .emailVerified(false)
                .build();

        EndUser savedUser = endUserRepository.save(newUser);

        String verificationToken = jwtService.generateEmailVerificationToken(savedUser.getEmail());

        String verificationLink = String.format(
                "http://localhost:8080/api/p/%s/auth/verify-email?token=%s",
                apiKey,
                verificationToken
        );

        String emailBody = "<h1>Chào mừng bạn đến với " + project.getName() + "!</h1>"
                + "<p>Vui lòng nhấp vào link sau để xác thực tài khoản của bạn (link có hiệu lực trong 15 phút):</p>"
                + "<a href=\"" + verificationLink + "\">Kích hoạt tài khoản</a>";
        emailService.sendHtmlEmail(savedUser.getEmail(), "Kích hoạt tài khoản của bạn", emailBody);

        return savedUser;
    }

    public AuthResponse login(String apiKey, EndUserLoginRequest request) {
        Project project = projectRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found for the given API key."));

        EndUser endUser = endUserRepository.findByEmailAndProject(request.getEmail(), project)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        // So khớp mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), endUser.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (!endUser.isEmailVerified()) {
            throw new BadCredentialsException("Account is not enabled. Please verify your email.");
        }
        if (endUser.isLocked()) {
            throw new BadCredentialsException("User account is locked.");
        }

        String token = jwtService.generateEndUserLoginToken(endUser.getEmail(), project);
        return AuthResponse.builder().accessToken(token).build();
    }

    @Transactional
    public void verifyEmail(String apiKey, String token) {
        Project project = projectRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        String email = jwtService.extractUsername(token);

        EndUser endUser = endUserRepository.findByEmailAndProject(email, project)
                .orElseThrow(() -> new InvalidTokenException("User not found for this token."));

        if (!jwtService.isVerificationTokenValid(token, endUser)) {
            throw new InvalidTokenException("Token is invalid or expired.");
        }

        endUser.setEmailVerified(true);
        endUserRepository.save(endUser);
    }

    public EndUserResponse getEndUserById(Long projectId, Long endUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        EndUser endUser = endUserRepository.findById(endUserId)
                .orElseThrow(() -> new UsernameNotFoundException("EndUser not found"));

        if (!endUser.getProject().getId().equals(project.getId())) {
            throw new AccessDeniedException("User does not belong to this project.");
        }
        return mapToEndUserResponse(endUser);
    }

    public List<EndUserResponse> getUsersByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        return endUserRepository.findByProject(project).stream()
                .map(this::mapToEndUserResponse)
                .collect(Collectors.toList());
    }

    // Cập nhật fullName enduser dành cho owner
    @Transactional
    public EndUserResponse updateUserDetails(Long projectId, Long endUserId, UpdateEndUserRequest request) {
        EndUser user = findUserAndVerifyProject(endUserId, projectId);

        user.setFullName(request.getFullName());
        user.getRoles().size();

        return mapToEndUserResponse(user);
    }

    // Cập nhật Role cho enduser dành cho owner
    @Transactional
    public EndUserResponse updateUserRoles(Long projectId, Long endUserId, UpdateEndUserRolesRequest request) {
        EndUser endUser = findUserAndVerifyProject(endUserId, projectId);

        List<ProjectRole> rolesToAssign = projectRoleRepository.findAllById(request.getRoleIds());

        for (ProjectRole role : rolesToAssign) {
            if (!role.getProject().getId().equals(projectId)) {
                throw new AccessDeniedException("Cannot assign role from a different project.");
            }
        }

        endUser.setRoles(new HashSet<>(rolesToAssign));
        EndUser updatedUser = endUserRepository.save(endUser);

        return mapToEndUserResponse(updatedUser);
    }

    @Transactional
    public EndUserResponse addRolesToUser(Long projectId, Long endUserId, UpdateEndUserRolesRequest request) {
        EndUser endUser = findUserAndVerifyProject(endUserId, projectId);

        List<ProjectRole> rolesToAssign = projectRoleRepository.findAllById(request.getRoleIds());

        for (ProjectRole role : rolesToAssign) {
            if (!role.getProject().getId().equals(projectId)) {
                throw new AccessDeniedException("Cannot assign role from a different project.");
            }
        }

        endUser.getRoles().addAll(rolesToAssign);
        EndUser updatedUser = endUserRepository.save(endUser);

        return mapToEndUserResponse(updatedUser);
    }

    @Transactional
    public void updateUserLockStatus(Long projectId, Long endUserId, boolean lockStatus) {
        EndUser endUser = findUserAndVerifyProject(endUserId, projectId);

        endUser.setLocked(lockStatus);
        endUserRepository.save(endUser);
    }

    @Transactional
    public void removeRoleFromUser(Long projectId, Long endUserId, Long roleId) {
        EndUser endUser = findUserAndVerifyProject(endUserId, projectId);

        ProjectRole roleToRemove = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalStateException("Role not found with id: " + roleId));

        if (endUser.getRoles().size() <= 1) {
            throw new IllegalStateException("Cannot remove the last role from a user. Assign a different role first.");
        }

        if (!endUser.getRoles().contains(roleToRemove)) {
            throw new IllegalStateException("User does not have the specified role.");
        }

        endUser.getRoles().remove(roleToRemove);
        endUserRepository.save(endUser);
    }

    public EndUserResponse getMyProfile(String email, String apiKey) {
        Project project = projectRepository.findByApiKey(apiKey).orElseThrow(() -> new ProjectNotFoundException("Project not found."));
        EndUser endUser = endUserRepository.findByEmailAndProject(email, project).orElseThrow(() -> new UsernameNotFoundException("User not found."));
        return mapToEndUserResponse(endUser);
    }

    @Transactional
    public EndUserResponse updateMyProfile(String email, String apiKey, UpdateMyProfileRequest request) {
        Project project = projectRepository.findByApiKey(apiKey).orElseThrow(() -> new ProjectNotFoundException("Project not found."));
        EndUser endUser = endUserRepository.findByEmailAndProject(email, project).orElseThrow(() -> new UsernameNotFoundException("User not found."));

        endUser.setFullName(request.getFullName());
        EndUser updatedUser = endUserRepository.save(endUser);
        return mapToEndUserResponse(endUser);
    }

    private final Cache<String, String> otpCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    // --- QUÊN MẬT KHẨU ---
    public void forgotPassword(String apiKey, ForgotPasswordRequest request) {
        Project project = projectRepository.findByApiKey(apiKey).orElseThrow(() -> new ProjectNotFoundException("Project not found."));
        EndUser endUser = endUserRepository.findByEmailAndProject(request.getEmail(), project).orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (!endUser.isEmailVerified()) {
            throw new IllegalStateException("Email has not been verified yet.");
        }

        String otp = generateOtp();
        otpCache.put(endUser.getEmail(), otp);

        String emailBody = "<h1>Yêu cầu đặt lại mật khẩu cho " + project.getName() + "</h1>"
                        + "<p>Mã OTP của bạn là: <b>" + otp + "</b></p>"
                        + "<p>Mã này sẽ hết hạn sau 5 phút.</p>";
        emailService.sendHtmlEmail(endUser.getEmail(), "Mã OTP đặt lại mật khẩu", emailBody);
    }

    @Transactional
    public void resetPassword(String apiKey, ResetPasswordRequest request) {
        Project project = projectRepository.findByApiKey(apiKey).orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        String storedOtp = otpCache.getIfPresent(request.getEmail());
        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new InvalidTokenException("Invalid or expired OTP.");
        }

        EndUser endUser = endUserRepository.findByEmailAndProject(request.getEmail(), project).orElseThrow(() -> new UsernameNotFoundException("User not found."));

        endUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        endUser.setPasswordLastChangedAt(Instant.now());
        endUserRepository.save(endUser);

        otpCache.invalidate(request.getEmail());
    }


    @Transactional
    public void changeMyPassword(String email, String apiKey, ChangePasswordRequest request) {
        Project project = projectRepository.findByApiKey(apiKey).orElseThrow(() -> new ProjectNotFoundException("Project not found."));
        EndUser endUser = endUserRepository.findByEmailAndProject(email, project).orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (!passwordEncoder.matches(request.getOldPassword(), endUser.getPassword())) {
            throw new BadCredentialsException("Incorrect old password.");
        }

        endUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        endUser.setPasswordLastChangedAt(Instant.now());
        endUserRepository.save(endUser);
    }

    // Xác thực token
    public TokenValidationResponse validateToken(String apiKey, String token) {
        Project project = projectRepository.findByApiKey(apiKey).orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        try {
            String audience = jwtService.extractAudience(token);
            if (audience == null || !audience.equals("END_USER_PROJECT:" + project.getId())) {
                return TokenValidationResponse.builder().valid(false).build();
            }

            String email = jwtService.extractUsername(token);
            EndUser endUser = endUserRepository.findByEmailAndProject(email, project).orElseThrow(() -> new UsernameNotFoundException("User not found."));

            if (jwtService.isEndUserLoginTokenValid(token, endUser) && endUser.isEnabled() && !endUser.isLocked()) {
                // Tính toán cấp bậc cao nhất
                int maxLevel = endUser.getRoles().stream()
                        .mapToInt(ProjectRole::getLevel)
                        .max()
                        .orElse(0);

                return TokenValidationResponse.builder()
                        .valid(true)
                        .email(endUser.getEmail())
                        .userId(endUser.getId())
                        .roles(endUser.getRoles().stream().map(ProjectRole::getName).collect(Collectors.toSet()))
                        .maxRoleLevel(maxLevel)
                        .build();
            }
        } catch (Exception e) {
            return TokenValidationResponse.builder().valid(false).build();
        }

        return TokenValidationResponse.builder().valid(false).build();
    }

    // --- CÁC PHƯƠMNG THỨC HỖ TRỢ ---

    private EndUser findUserAndVerifyProject(Long projectId, Long endUserId) {
        EndUser endUser = endUserRepository.findById(endUserId)
                .orElseThrow(() -> new UsernameNotFoundException("EndUser not found with id: " + endUserId));

        if (!endUser.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("This user does not belong to the specified project.");
        }

        return endUser;
    }

    private void verifyProjectOwnership(String ownerEmail, Long projectId) {
        Owner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found."));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        if (!project.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You do not have permission to access this project's users.");
        }
    }

    private EndUserResponse mapToEndUserResponse(EndUser endUser) {
        EndUserResponse response = new EndUserResponse();
        response.setId(endUser.getId());
        response.setFullName(endUser.getFullName());
        response.setEmail(endUser.getEmail());
        response.setEmailVerified(endUser.isEmailVerified());
        response.setLocked(endUser.isLocked());
        response.setRoles(
                endUser.getRoles().stream()
                        .map(ProjectRole::getName)
                        .collect(Collectors.toSet())
        );
        return response;
    }
}
