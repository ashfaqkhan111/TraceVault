package com.tracevault.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tracevault.backend.enms.AccountStatus;

public class UserResponseDTO {

    private UUID userId;
    private String accountCode;
    private String fullName;
    private String email;
    private AccountStatus accountStatus;
    private OffsetDateTime createdAt;

    public UserResponseDTO(){

    }

    public UserResponseDTO(UUID userId, String accountCode, String fullName, String email, AccountStatus accountStatus,OffsetDateTime createdAt){
        this.userId = userId;
        this.accountCode = accountCode;
        this.fullName = fullName;
        this.email = email;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;

    }

    public UUID getUserId(){
        return userId;
    }
    public String getAccountCode(){
        return accountCode;
    }
    public String getFullName(){
        return fullName;
    }
    public String getEmail(){
        return email;
    }
    public AccountStatus getAccountStatus(){
        return accountStatus;
    }
    public OffsetDateTime getCreatedAt(){
        return createdAt;
    }
}
