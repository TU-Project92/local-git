package com.example.project.backend.dto.request.documentVersion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDocumentVersionRequest {

    //@NotBlank(message = "Id of the document is required")
    private Long documentId;

}