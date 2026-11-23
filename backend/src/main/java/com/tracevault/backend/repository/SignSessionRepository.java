package com.tracevault.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.SignInSession;

public interface SignSessionRepository extends JpaRepository<SignInSession, UUID>{

    List<SignInSession> findByUser_UserId(UUID userId);

    List<SignInSession> findByUser_UserIdAndSignOutAtIsNUll(UUID userId);

    
} 
