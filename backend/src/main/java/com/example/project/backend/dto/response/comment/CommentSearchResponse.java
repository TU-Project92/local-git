package com.example.project.backend.dto.response.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentSearchResponse {
    private Long id;
    private String title;
    private Long versionId;
    private String username;
    private String createdAt;
    private String comment;
}
