package com.tracevault.backend.service;

import com.tracevault.backend.entity.User;
import com.tracevault.backend.repository.UserRepository;
import com.tracevault.backend.dto.UserResponseDTO;
import org.springframework.stereotype.Service;


import java.util.UUID;
import java.util.List;

@Service 
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User getUserById(UUID userId){
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));
    }

    public boolean emailExist(String email){
        return userRepository.existByEmail(email);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public UserResponseDTO convertToDTO(User user){

        return new UserResponseDTO(
            user.getUserId(),
            user.getAccountCode(),
            user.getFullName(),
            user.getEmail(),
            user.getAccountStatus(),
            user.getCreatedAt()

        );

    }
}
