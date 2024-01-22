package com.harrish.readwritedatasource.controller;

import com.harrish.readwritedatasource.dto.UserRequest;
import com.harrish.readwritedatasource.dto.UserResponse;
import com.harrish.readwritedatasource.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void addUser(@RequestBody UserRequest userRequest) {
        userService.addUser(userRequest);
    }

    @GetMapping
    ResponseEntity<List<UserResponse>> finaAllUsers() {
        return ResponseEntity.ok(userService.finaAllUser());
    }
}
