package com.example.project.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteAdminRequest {

    @NotBlank(message = "Username is required.")
    private String username;

}
