package com.example.project.backend.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSearchResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean active;
}