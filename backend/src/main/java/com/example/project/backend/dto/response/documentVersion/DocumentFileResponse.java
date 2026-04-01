package com.example.project.backend.dto.response.documentVersion;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentFileResponse {
    private String fileName;
    private String contentType;
    private byte[] content;
}
