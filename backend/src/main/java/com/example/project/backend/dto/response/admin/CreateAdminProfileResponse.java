package com.example.project.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateAdminProfileResponse {

    private Long adminProfileId;
    private Long linkedUserId;
    private String adminUsername;
    private String adminEmail;
    private String message;

}
