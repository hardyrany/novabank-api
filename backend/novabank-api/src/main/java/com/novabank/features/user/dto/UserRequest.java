package com.novabank.features.user.dto;

import java.util.Set;
import com.novabank.features.user.enums.Role;

public class UserRequest {

    private String email;
    private String password;
    private Set<Role> roles;

    public UserRequest() {}

    public UserRequest(String email, String password, Set<Role> roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

}
