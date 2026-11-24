package com.tracevault.backend.controller;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.tracevault.backend.dto.RegisterRequest;
import com.tracevault.backend.dto.UserResponseDTO;
import com.tracevault.backend.service.AuthService;
import com.tracevault.backend.dto.LoginRequest;
import com.tracevault.backend.entity.SignInSession;
import com.tracevault.backend.entity.User;
import com.tracevault.backend.repository.UserRepository;
import com.tracevault.backend.service.LoginService;
import com.tracevault.backend.service.SignInSessionService;
import com.tracevault.backend.entity.SignInSession;

@RestController 
@RequestMapping ("/api/auth")
@Validated 
public class AuthController {

    private final AuthService authService;
    private final LoginService loginService;
private final UserRepository userRepository;
private final SignInSessionService signInSessionService;

private final HttpSessionSecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();

            public AuthController(
                AuthService authService,
                LoginService loginService,
                UserRepository userRepository,
                SignInSessionService signInSessionService) {

            this.authService = authService;
            this.loginService = loginService;
            this.userRepository = userRepository;
            this.signInSessionService = signInSessionService;
        }

    @PostMapping ("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequest request){
        UserResponseDTO response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
public ResponseEntity<UserResponseDTO> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) {

    // 1. Verify email and password
    Authentication authentication = loginService.authenticate(request);

    // 2. Create authenticated security context
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    // 3. Save authentication in HTTP session
    securityContextRepository.saveContext(
            context,
            httpRequest,
            httpResponse
    );
    User user = userRepository
            .findByEmailIgnoreCase(request.email().trim())
            .orElseThrow();

    user.setLastSignInAt(OffsetDateTime.now());
    userRepository.save(user);

    String userAgent = httpRequest.getHeader("User-Agent");

  SignInSession signInSession = signInSessionService.recordSignIn(
        user.getUserId(),
        "Unknown",
        userAgent != null ? userAgent : "Unknown",
        httpRequest.getRemoteAddr()
);

httpRequest.getSession().setAttribute(
        "TRACEVAULT_SIGN_IN_SESSION_ID",
        signInSession.getSessionId()
);
    UserResponseDTO response = new UserResponseDTO(
            user.getUserId(),
            user.getAccountCode(),
            user.getFullName(),
            user.getEmail(),
            user.getAccountStatus(),
            user.getCreatedAt()
    );

    return ResponseEntity.ok(response);
}

@PostMapping("/logout")
public ResponseEntity<Map<String, String>> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) {

    HttpSession session = request.getSession(false);

    if (session != null) {

        Object sessionId = session.getAttribute(
                "TRACEVAULT_SIGN_IN_SESSION_ID"
        );

        if (sessionId instanceof UUID uuid) {
            signInSessionService.recordSignOut(uuid);
        }

        new SecurityContextLogoutHandler()
                .logout(request, response, authentication);
    }

    return ResponseEntity.ok(
            Map.of("message", "Logged out successfully")
    );
}
    
}
