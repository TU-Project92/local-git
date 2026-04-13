package com.example.project.backend.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserActivationResponse {

    private Long userId;
    private String username;
    private boolean active;
    private String message;

}
