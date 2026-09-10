package com.novabank.infra.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.novabank.features.user.repository.UserRepository;

@Service 
public class UserDetailsServiceImpl implements UserDetailsService {

    private  final UserRepository userRepository;

    public  UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
}
