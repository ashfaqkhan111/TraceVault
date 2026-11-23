package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tracevault.backend.entity.SignInSession;
import com.tracevault.backend.service.SignInSessionService;

@RestController 
@RequestMapping ("/api/session")
public class SignInSessionController {

    private final SignInSessionService sessionService;

    public SignInSessionController(SignInSessionService sessionService){
        this.sessionService = sessionService;
    }

    @GetMapping ("/user/{userId}")
    public ResponseEntity<List<SignInSession>> getUserSessions(@PathVariable UUID userId){
        return ResponseEntity.ok(sessionService.getUserSessions(userId));
    }

    @PutMapping ("/{sessionId}/sign-out")
    public ResponseEntity<SignInSession> recordSignOut(@PathVariable UUID sessionId){
        return ResponseEntity.ok(sessionService.recordSignOut(sessionId));
    }
    
}
