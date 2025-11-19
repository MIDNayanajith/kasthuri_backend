package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.entity.UserEntity;
import com.enterprise.bms.enterprise_bms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("Profile not found with email:"+email));
        return User.builder()
                .username(existingUser.getEmail())
                .password(existingUser.getPassword())
                .authorities("ROLE_" + existingUser.getRole())
                .build();
    }
}
