package com.novabank.features.user.mapper;

import org.springframework.stereotype.Component;
import com.novabank.features.user.dto.UserRequest;
import com.novabank.features.user.dto.UserResponse;
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

    public UserResponse toResponse(User user) {

        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setIsActive(user.getIsActive());
        userResponse.setRoles(user.getRoles());

        return userResponse;
    }

}
