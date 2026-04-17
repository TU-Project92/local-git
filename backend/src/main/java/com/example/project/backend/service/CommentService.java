package com.example.project.backend.service;

import com.example.project.backend.dto.request.comment.CommentSearchByUserRequest;
import com.example.project.backend.dto.request.comment.CommentSearchByVersionRequest;
import com.example.project.backend.dto.response.comment.CommentSearchResponse;
import com.example.project.backend.model.entity.DocumentVersion;
import com.example.project.backend.model.entity.User;
import com.example.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);


    @Transactional(readOnly = true)
    public List<CommentSearchResponse> searchCommentsByVersion(CommentSearchByVersionRequest request){

        DocumentVersion version = documentVersionRepository.findByIdAndDocumentId(request.getVersionId(), request.getDocumentId())
                .orElseThrow(() -> {
                    logger.error("Cannot access comments of version - version with id {} of document with id {} not found", request.getVersionId(), request.getDocumentId());
                    return new IllegalArgumentException("No document version found");
                });

        logger.info("Successfully accessed comments of version with id {} of document with id {}", request.getVersionId(), request.getDocumentId());

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
                .orElseThrow(() -> {
                    logger.error("Cannot access comments of user - user with username {} not found", request.getUsername());
                    return new IllegalArgumentException("No such user found");
                });

        logger.info("Successfully accessed comments of user with id {}", user.getId());

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
