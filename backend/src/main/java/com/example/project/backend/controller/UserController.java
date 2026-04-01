package com.example.project.backend.controller;

import com.example.project.backend.dto.response.user.UserSearchResponse;
import com.example.project.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(userService.searchUsers(search));
    }
}