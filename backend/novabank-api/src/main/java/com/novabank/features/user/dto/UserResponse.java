package com.novabank.features.user.dto;

import java.util.Set;
import java.util.UUID;
import com.novabank.features.user.enums.Role;

public class UserResponse {

    private UUID id;
    private String email;
    private Boolean isActive;
    private Set<Role> roles;
    
}
