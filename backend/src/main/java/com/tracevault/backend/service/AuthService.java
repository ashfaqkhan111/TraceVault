package com.tracevault.backend.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tracevault.backend.dto.RegisterRequest;
import com.tracevault.backend.dto.UserResponseDTO;
import com.tracevault.backend.entity.User;
import com.tracevault.backend.enms.AccountStatus;
import com.tracevault.backend.enms.AuthProvider;
import com.tracevault.backend.repository.UserRepository;


@Service 
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSettingService userSettingService;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserSettingService userSettingService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSettingService = userSettingService;
    }
    
    @Transactional 
    public UserResponseDTO register(RegisterRequest request){
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if(userRepository.existsByEmail(email)){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"An account with this email is already exists.");
        }

        User user = new User();

        user.setFullName(request.fullName().trim());
        user.setEmail(email);

        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user.setAccountCode(generateAccountCode());
        user.setAuthProvider(AuthProvider.EMAIL);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setTermsAcceptedAt(OffsetDateTime.now());

        User savedUser = userRepository.save(user);

        userSettingService.createUserSettings(savedUser.getUserId());

        return new UserResponseDTO(savedUser.getUserId(),savedUser.getAccountCode(),
                                    savedUser.getFullName(),savedUser.getEmail(), savedUser.getAccountStatus(), savedUser.getCreatedAt());

    }

    private String generateAccountCode(){
        String accountCode;

        do{
            int number = secureRandom.nextInt(1_000_000);

            accountCode = String.format("TV-%d-%06d", Year.now().getValue(),number);
        }while(userRepository.existByAccountCode(accountCode));

        return accountCode;
    }
}
