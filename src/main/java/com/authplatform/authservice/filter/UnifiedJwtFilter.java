package com.authplatform.authservice.filter;

import com.authplatform.authservice.repository.EndUserRepository;
import com.authplatform.authservice.repository.ProjectRepository;
import com.authplatform.authservice.service.JwtService;
import com.authplatform.authservice.service.OwnerDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UnifiedJwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final OwnerDetailsService ownerDetailsService;
    private final EndUserRepository endUserRepository;
    private final ProjectRepository projectRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);
        final String audience = jwtService.extractAudience(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;

            // Xác định loại user dựa vào audience
            if ("OWNER_PLATFORM".equals(audience)) {
                userDetails = ownerDetailsService.loadUserByUsername(userEmail);
            } else if (audience != null && audience.startsWith("END_USER_PROJECT:")) {
                Long projectId = Long.parseLong(audience.split(":")[1]);
                userDetails = projectRepository.findById(projectId)
                        .flatMap(project -> endUserRepository.findByEmailAndProject(userEmail, project))
                        .orElse(null);
            }

            if (userDetails != null) {
                boolean isTokenValid = "OWNER_PLATFORM".equals(audience)
                        ? jwtService.isOwnerLoginTokenValid(jwt, userDetails)
                        : jwtService.isEndUserLoginTokenValid(jwt, userDetails);

                if (isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
