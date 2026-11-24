package com.tracevault.backend.service;

import java.util.Locale;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import com.tracevault.backend.dto.LoginRequest;

@Service 
public class LoginService {

    private final AuthenticationManager authenticationManager;

    private LoginService(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }
    
    public Authentication authenticate(LoginRequest request){
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
    }
}