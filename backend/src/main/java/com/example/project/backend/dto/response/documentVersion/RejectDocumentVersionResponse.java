package com.example.project.backend.dto.response.documentVersion;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RejectDocumentVersionResponse {
    private Long documentId;
    private String title;
    private Integer version;
    private String rejectedBy;
    private String status;
    private String comment;
}
