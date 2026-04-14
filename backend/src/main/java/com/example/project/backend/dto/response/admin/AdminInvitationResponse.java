package com.example.project.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminInvitationResponse {

    private Long invitationId;
    private String senderUsername;
    private String recipientUsername;
    private String status;
    private String message;
}
