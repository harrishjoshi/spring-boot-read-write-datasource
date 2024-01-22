package com.harrish.readwritedatasource.service;

import com.harrish.readwritedatasource.dto.UserRequest;
import com.harrish.readwritedatasource.dto.UserResponse;
import com.harrish.readwritedatasource.model.User;
import com.harrish.readwritedatasource.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser(UserRequest userRequest) {
        var user = new User();
        BeanUtils.copyProperties(userRequest, user);

        userRepository.save(user);
    }

    public List<UserResponse> finaAllUser() {
        return userRepository.findAll()
                .stream().map(this::mapToUserResponse)
                .toList();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .address(user.getAddress())
                .email(user.getEmail())
                .build();
    }
}
