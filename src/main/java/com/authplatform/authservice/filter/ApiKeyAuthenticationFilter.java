package com.authplatform.authservice.filter;

import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.repository.ProjectRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String apiKey = request.getHeader("X-API-Key");
        final String apiSecret = request.getHeader("X-API-Secret");

        if (apiKey == null || apiSecret == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        projectRepository.findByApiKey(apiKey).ifPresent(project -> {
            if (passwordEncoder.matches(apiSecret, project.getHashedProjectSecret())) {
                Owner owner = project.getOwner();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        owner,
                        null,
                        owner.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        });

        filterChain.doFilter(request, response);
    }
}