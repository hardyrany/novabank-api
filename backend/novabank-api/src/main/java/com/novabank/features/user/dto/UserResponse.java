package com.novabank.features.user.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.novabank.features.user.enums.Role;

public class UserResponse {

    private UUID id;
    private String email;
    private Boolean isActive;
    private Set<Role> roles;

    public UserResponse() {}

    public UserResponse(UUID id, String email, Boolean isActive, Set<Role> roles) {
        this.id = id;
        this.email = email;
        this.isActive = isActive;
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Set<Role> getRoles() {
        return new HashSet<>(roles);
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

}
