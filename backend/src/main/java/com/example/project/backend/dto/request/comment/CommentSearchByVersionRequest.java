package com.example.project.backend.dto.request.comment;

import lombok.Data;

@Data
public class CommentSearchByVersionRequest {

    //@NotBlank(message = "Document Id is required")
    private Long documentId;

    //@NotBlank(message = "Version Id is required")
    private Long versionId;

}
