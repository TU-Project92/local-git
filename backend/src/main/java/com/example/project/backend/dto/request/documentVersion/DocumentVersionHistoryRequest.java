package com.example.project.backend.dto.request.documentVersion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentVersionHistoryRequest {

    @NotNull(message = "Document Id is required")
    private Long documentId;
}
