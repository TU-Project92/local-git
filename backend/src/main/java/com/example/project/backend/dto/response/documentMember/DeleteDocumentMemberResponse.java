package com.example.project.backend.dto.response.documentMember;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteDocumentMemberResponse {
    private String username;
    private String role;
    private Long documentId;
    private String message;
}
