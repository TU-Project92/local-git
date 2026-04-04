package com.example.project.backend.service;

import com.example.project.backend.dto.request.comment.CommentSearchByUserRequest;
import com.example.project.backend.dto.request.comment.CommentSearchByVersionRequest;
import com.example.project.backend.dto.response.comment.CommentSearchResponse;
import com.example.project.backend.model.entity.DocumentVersion;
import com.example.project.backend.model.entity.User;
import com.example.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentMemberRepository documentMemberRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public List<CommentSearchResponse> searchCommentsByVersion(CommentSearchByVersionRequest request){

        DocumentVersion version = documentVersionRepository.findByIdAndDocumentId(request.getVersionId(), request.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("No document version found"));

        return commentRepository.findAllByVersion(version)
                .stream()
                .map(comment -> new CommentSearchResponse(
                        comment.getId(),
                        comment.getVersion().getDocument().getTitle(),
                        comment.getVersion().getId(),
                        comment.getUser().getUsername(),
                        comment.getCreatedAt().toString(),
                        comment.getCommentText()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentSearchResponse> searchCommentsByUser(CommentSearchByUserRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("No such user found"));

        return commentRepository.findAllByUser(user)
                .stream()
                .map(comment -> new CommentSearchResponse(
                        comment.getId(),
                        comment.getVersion().getDocument().getTitle(),
                        comment.getVersion().getId(),
                        comment.getUser().getUsername(),
                        comment.getCreatedAt().toString(),
                        comment.getCommentText()
                ))
                .toList();
    }
}
