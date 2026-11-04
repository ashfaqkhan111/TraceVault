package com.tracevault.backend.config;

import com.tracevault.backend.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Runs on every request: reads the “Authorization: Bearer <token>” header.
 * If the token is valid, Spring knows who the user is (used by @AuthenticationPrincipal).
 *
 * Intentionally NOT annotated with @Component, because it is defined in SecurityConfig
 * (if annotated with @Component, this filter could be applied twice).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwtService, UserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            jwtService.validate(header.substring(7).trim())
                    .flatMap(users::findById)
                    .ifPresent(user -> {
                        var auth = new UsernamePasswordAuthenticationToken(
                                user.getId(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(request, response);
    }
}
