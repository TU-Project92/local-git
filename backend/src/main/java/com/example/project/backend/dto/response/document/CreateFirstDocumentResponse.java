package com.example.project.backend.dto.response.document;

import com.example.project.backend.model.enums.DocumentRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateFirstDocumentResponse {
    private Long id;
    private String title;
    private String description;
    private String createdByUsername;
    private DocumentRole role;
    private String message;
}