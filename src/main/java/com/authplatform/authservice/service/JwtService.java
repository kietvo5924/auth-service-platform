package com.authplatform.authservice.service;

import com.authplatform.authservice.model.EndUser;
import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.model.Project;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final long emailVerificationExpiration = 15 * 60 * 1000;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractAudience(String token) {
        // Lấy danh sách audience từ claims
        final Set<String> audienceSet = extractClaim(token, Claims::getAudience);

        if (audienceSet == null || audienceSet.isEmpty()) {
            return null;
        }

        return audienceSet.stream().findFirst().orElse(null);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // --- TẠO TOKEN ĐĂNG NHẬP ---
    public String generateOwnerLoginToken(Owner owner) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("aud", "OWNER_PLATFORM");
        claims.put("role", owner.getRole().name());

        return Jwts.builder()
                .claims(claims)
                .subject(owner.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Tạo token đăng nhập cho EndUser
    public String generateEndUserLoginToken(String username, Project project) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("apiKey", project.getApiKey());

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .audience().add("END_USER_PROJECT:" + project.getId()).and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // --- TẠO TOKEN XÁC THỰC EMAIL ---
    public String generateEmailVerificationToken(String email) {
        return Jwts.builder()
                .subject(email)
                .audience().add("EMAIL_VERIFICATION").and()
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(emailVerificationExpiration)))
                .signWith(getSigningKey())
                .compact();
    }

    // --- CÁC HÀM XÁC THỰC TOKEN ---

    // Chỉ kiểm tra email và ngày hết hạn
    public boolean isVerificationTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Hàm phức tạp: Ngoài kiểm tra cơ bản, nó còn kiểm tra `passwordLastChangedAt` nếu user là `Owner`.
     * Dùng cho token ĐĂNG NHẬP của Owner.
     */
    public boolean isOwnerLoginTokenValid(String token, UserDetails userDetails) {
        if (!isVerificationTokenValid(token, userDetails)) {
            return false;
        }

        if (userDetails instanceof Owner owner) {
            final Date tokenIssuedAt = extractClaim(token, Claims::getIssuedAt);
            final Instant passwordLastChanged = owner.getPasswordLastChangedAt();

            if (passwordLastChanged != null) {
                return !tokenIssuedAt.toInstant().isBefore(passwordLastChanged);
            }
        }
        return true;
    }

    public boolean isEndUserLoginTokenValid(String token, UserDetails userDetails) {
        if (!isOwnerLoginTokenValid(token, userDetails)) {
            return false;
        }

        if (userDetails instanceof EndUser endUser) {
            final Date tokenIssuedAt = extractClaim(token, Claims::getIssuedAt);
            final Instant passwordLastChanged = endUser.getPasswordLastChangedAt();
            if (passwordLastChanged != null) {
                return !tokenIssuedAt.toInstant().isBefore(passwordLastChanged);
            }
        }
        return true;
    }
}
