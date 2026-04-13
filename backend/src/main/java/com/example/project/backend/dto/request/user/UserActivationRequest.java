package com.example.project.backend.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserActivationRequest {

    @NotNull(message = "User id is required.")
    private Long userId;

    @NotBlank(message = "Reason for account activation is required.")
    private String reason;
}
