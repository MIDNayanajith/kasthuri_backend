package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.AuthDTO;
import com.enterprise.bms.enterprise_bms.dto.UserDTO;
import com.enterprise.bms.enterprise_bms.entity.UserEntity;
import com.enterprise.bms.enterprise_bms.repository.UserRepository;
import com.enterprise.bms.enterprise_bms.utill.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UserDTO registerUser(UserDTO userDTO){
        UserEntity newUser = toEntity(userDTO);
        newUser = userRepository.save(newUser);
        return  toDTO(newUser);
    }

    // Add to UserService.java

    public List<UserDTO> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UserDTO updateUser(UserDTO userDTO) {
        UserEntity existingUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Update fields
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setRole(userDTO.getRole().toUpperCase());
        existingUser.setMobileNo(userDTO.getMobileNo());
        existingUser.setProfileImg(userDTO.getProfileImg());
        existingUser.setIsActive(userDTO.getIsActive());
        existingUser.setUpdatedAt(LocalDateTime.now());
        // Only update password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        UserEntity updatedUser = userRepository.save(existingUser);
        return toDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
    //HELPER METHODS

    public UserEntity toEntity(UserDTO userDTO){
        return UserEntity.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(userDTO.getRole().toUpperCase())
                .email(userDTO.getEmail())
                .mobileNo(userDTO.getMobileNo())
                .profileImg(userDTO.getProfileImg())
                .isActive(true)
                .createdAt(userDTO.getCreatedAt())
                .updatedAt(userDTO.getUpdatedAt())
                .build();
    }

    public UserDTO toDTO(UserEntity userEntity){
        return UserDTO.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .role(userEntity.getRole())
                .email(userEntity.getEmail())
                .mobileNo(userEntity.getMobileNo())
                .profileImg(userEntity.getProfileImg())
                .isActive(userEntity.getIsActive())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .build();
    }

    public boolean isAccountActive(String email){
        return userRepository.findByEmail(email)
                .map(UserEntity::getIsActive)
                .orElse(false);
    }

    //GET CURRENT USER
    public UserEntity getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new UsernameNotFoundException("User not authenticated");  // CHANGED: Better exception and check
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(()->new UsernameNotFoundException("User not found with email:" + authentication.getName()));
    }

    public UserDTO getPublicProfile(String email){
        UserEntity currentUser = null;
        if(email == null){
            currentUser = getCurrentUser();
        }
        else{
            currentUser = userRepository.findByEmail(email)
                    .orElseThrow(()->new UsernameNotFoundException("User not found with email:"+email));
        }
        return UserDTO.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .role(currentUser.getRole())
                .email(currentUser.getEmail())
                .mobileNo(currentUser.getMobileNo())
                .profileImg(currentUser.getProfileImg())
                .isActive(currentUser.getIsActive())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

    public Map<String,Object> authenticateAndGenerateToken(AuthDTO authDto){
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword()));

            // Generate JWT Token - FIXED: now returns the actual token variable
            String token = jwtUtil.generateToken(authDto.getEmail());
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDto.getEmail())
            );
        }
        catch(Exception e){
            throw new RuntimeException("Invalid email or Password");
        }
    }

}
