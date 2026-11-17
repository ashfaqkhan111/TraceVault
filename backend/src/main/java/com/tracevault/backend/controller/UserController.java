package com.tracevault.backend.controller;

import com.tracevault.backend.entity.User;
import com.tracevault.backend.service.UserService;
import com.tracevault.backend.dto.UserResponseDTO;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController 
@RequestMapping ("/api/users")
public class UserController {
    
    private final UserService userService;

    public UserController (UserService userService){
        this.userService = userService;
    }

    @GetMapping 
    public List<UserResponseDTO> getAllUserResponse(){
        return userService.getAllUsers().stream().map(userService::convertToDTO).toList();

    }

    @GetMapping ("/{id}")
    public UserResponseDTO getUserById(@PathVariable UUID id){
        User user = userService.getUserById(id);

        return userService.convertToDTO(user);
    }


}
