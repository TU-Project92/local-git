package com.example.project.backend.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdminProfileRequest {

    @NotNull(message = "Invitation id is required.")
    private Long invitationId;

    @NotBlank(message =  "Admin username is required.")
    private String adminUsername;

    @NotBlank(message = "Admin email is required.")
    @Email(message = "Invalid email format.")
    private String adminEmail;

    @NotBlank(message = "Admin password is required.")
    private String adminPassword;
}

