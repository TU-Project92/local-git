package com.example.project.backend.controller;

import com.example.project.backend.dto.request.comment.CommentSearchByUserRequest;
import com.example.project.backend.dto.request.comment.CommentSearchByVersionRequest;
import com.example.project.backend.dto.response.comment.CommentSearchResponse;
import com.example.project.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/searchByVersion")
    public ResponseEntity<List<CommentSearchResponse>> searchByDocument(
            @RequestBody @Valid CommentSearchByVersionRequest request
            ){
        List<CommentSearchResponse> responses = commentService.searchCommentsByVersion(request);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/searchByUser")
    public ResponseEntity<List<CommentSearchResponse>> searchByUser(
            @RequestBody @Valid CommentSearchByUserRequest request
            ){
        List<CommentSearchResponse> responses = commentService.searchCommentsByUser(request);

        return ResponseEntity.ok(responses);
    }

}
