package com.example.project.backend.dto.user.response;

import com.example.project.backend.model.enums.SystemRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponse {

    private Long id;
    private String username;
    private String email;
    private SystemRole systemRole;
    private String message;
}