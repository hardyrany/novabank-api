package com.novabank.features.user.dto;

import java.util.Set;
import com.novabank.features.user.enums.Role;

public class UserRequest {

    private String email;
    private String password;
    private Set<Role> roles;

}
