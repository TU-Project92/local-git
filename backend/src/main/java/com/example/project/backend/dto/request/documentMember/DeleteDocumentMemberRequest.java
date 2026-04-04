package com.example.project.backend.dto.request.documentMember;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteDocumentMemberRequest {

    @NotBlank(message = "You must specify the user")
    private String username;

    //@NotBlank(message = "Document Id is required")
    private Long documentId;
}
