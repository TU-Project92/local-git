package com.example.project.backend.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserRegisterResponse {

    private Long id;
    private String username;
    private String email;
    private String message;
}
