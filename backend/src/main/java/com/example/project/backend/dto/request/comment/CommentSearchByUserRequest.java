package com.example.project.backend.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentSearchByUserRequest {

    @NotBlank(message = "Username is required")
    private String username;
}
