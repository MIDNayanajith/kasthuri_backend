package com.enterprise.bms.enterprise_bms.service;

import com.enterprise.bms.enterprise_bms.dto.UserDTO;
import com.enterprise.bms.enterprise_bms.entity.UserEntity;
import com.enterprise.bms.enterprise_bms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO registerUser(UserDTO userDTO){
        UserEntity newUser = toEntity(userDTO);
        newUser = userRepository.save(newUser);
        return  toDTO(newUser);
    }

    //HELPER METHODS

    public UserEntity toEntity(UserDTO userDTO){
        return UserEntity.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .role(userDTO.getRole())
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
}
