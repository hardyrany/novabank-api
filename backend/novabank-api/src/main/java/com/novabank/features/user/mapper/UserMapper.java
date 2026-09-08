package com.novabank.features.user.mapper;

import org.springframework.stereotype.Component;
import com.novabank.features.user.dto.UserRequest;
import com.novabank.features.user.entity.User;

@Component
public class UserMapper {

    public User toEntity(UserRequest userRequest) {

        User user = new User();

        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());
        user.setRoles(userRequest.getRoles());
        user.setIsActive(true);

        return user;
    }

}
