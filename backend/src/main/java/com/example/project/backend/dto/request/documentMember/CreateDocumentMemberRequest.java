package com.example.project.backend.dto.request.documentMember;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDocumentMemberRequest {

    //@NotBlank(message = "You must specify the id of the document")
    private Long documentId;

    @NotBlank(message = "You must specify the role")
    private String role;

    @NotBlank(message = "You must specify the owner of the document")
    private String owner;

    @NotBlank(message = "You must specify the user")
    private String username;

}
