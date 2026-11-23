package com.tracevault.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.SignInSession;
import com.tracevault.backend.entity.User;
import com.tracevault.backend.repository.SignSessionRepository;
import com.tracevault.backend.repository.UserRepository;

@Service 
public class SignInSessionService {

    private final SignSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SignInSessionService(SignSessionRepository sessionRepository, UserRepository userRepository){
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional 
    public SignInSession recordSignIn(UUID userId, String device, String browser, String ipAddress){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not fount"));

        SignInSession session = new SignInSession();

        session.setUser(user);
        session.setDevice(device);
        session.setBrowser(browser);
        session.setIpAddress(ipAddress);
        session.setSignedInAt(OffsetDateTime.now());

        return sessionRepository.save(session);
        
    }

    public List<SignInSession> getUserSessions(UUID userId){
        return sessionRepository.findByUser_UserId(userId);
    }

    public List<SignInSession> getActiveSessions(UUID userId){
        return sessionRepository.findByUser_UserIdAndSignedOutAtIsNull(userId);
    }

    @Transactional 
    public SignInSession recordSignOut(UUID sessionId){
        SignInSession session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found."));

        if (session.getSignedOutAt() != null){
            throw new RuntimeException("Session already signout.");
        }

        session.setSignedOutAt(OffsetDateTime.now());

        return sessionRepository.save(session);
    }
}
