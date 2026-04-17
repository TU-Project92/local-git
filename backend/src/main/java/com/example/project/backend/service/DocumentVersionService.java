package com.example.project.backend.service;

import com.example.project.backend.dto.request.documentVersion.ApproveDocumentVersionRequest;
import com.example.project.backend.dto.request.documentVersion.CreateDocumentVersionRequest;
import com.example.project.backend.dto.request.documentVersion.RejectDocumentVersionRequest;
import com.example.project.backend.dto.response.documentVersion.ApproveDocumentVersionResponse;
import com.example.project.backend.dto.response.documentVersion.CreateDocumentVersionResponse;
import com.example.project.backend.dto.response.documentVersion.DocumentFileResponse;
import com.example.project.backend.dto.response.documentVersion.DocumentVersionHistoryResponse;
import com.example.project.backend.dto.response.documentVersion.RejectDocumentVersionResponse;
import com.example.project.backend.model.entity.*;
import com.example.project.backend.dto.response.documentVersion.*;
import com.example.project.backend.model.entity.Document;
import com.example.project.backend.model.entity.DocumentMember;
import com.example.project.backend.model.entity.DocumentVersion;
import com.example.project.backend.model.entity.User;
import com.example.project.backend.model.enums.DocumentRole;
import com.example.project.backend.model.enums.NotificationType;
import com.example.project.backend.model.enums.VersionStatus;
import com.example.project.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentVersionService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentMemberRepository documentMemberRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final DocumentFileStorageService documentFileStorageService;
    private static final Logger logger = LoggerFactory.getLogger(DocumentVersionService.class);

    @Transactional
    public CreateDocumentVersionResponse createDocumentVersion(
            CreateDocumentVersionRequest request,
            MultipartFile file,
            String username
    ) {
        String errorMsg = "Cannot create document version -";

        if (file == null || file.isEmpty()) {
            logger.error("{} no file found", errorMsg);
            throw new IllegalArgumentException("File is required");
        }

        User loggedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("{} logged user with username {} not found", errorMsg, username);
                    return new IllegalArgumentException("Logged user not found");
                });

        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> {
                    logger.error("{} document with id {} not found", errorMsg, request.getDocumentId());
                    return new IllegalArgumentException("Document not found");
                });

        DocumentMember documentMember = documentMemberRepository.findByDocumentAndUser(document, loggedUser)
                .orElseThrow(() -> {
                    logger.error("{} user with id {} does not have access to document with id {}", errorMsg, loggedUser.getId(), document.getId());
                    return new IllegalArgumentException("You don't have access to the document");
                });

        if (documentMember.getRole() != DocumentRole.AUTHOR && documentMember.getRole() != DocumentRole.OWNER) {
            logger.error("{} user with id {} is not an owner or an author of document with id {}", errorMsg, loggedUser.getId(), document.getId());
            throw new IllegalArgumentException("You don't have the rights to make changes to this document");
        }

        DocumentVersion currentActiveVersion = document.getActiveVersion();
        if (currentActiveVersion == null) {
            logger.error("{} document with id {} has no active version", errorMsg, document.getId());
            throw new IllegalArgumentException("Document has no active version");
        }

        document.setNumberOfVersions(document.getNumberOfVersions() + 1);
        int newVersionNumber = document.getNumberOfVersions();

        DocumentFileStorageService.StoredFileData storedFile =
                documentFileStorageService.saveFile(document.getId(), newVersionNumber, file);

        DocumentVersion newVersion = DocumentVersion.builder()
                .document(document)
                .versionNumber(newVersionNumber)
                .filePath(storedFile.filePath())
                .originalFileName(storedFile.originalFileName())
                .contentType(storedFile.contentType())
                .fileSize(storedFile.fileSize())
                .status(VersionStatus.DRAFT)
                .createdBy(loggedUser)
                .parentVersion(currentActiveVersion)
                .build();

        DocumentVersion savedVersion = documentVersionRepository.save(newVersion);

        logger.info("Successful upload of new version {} of document with id {} by user with id {}", savedVersion.getVersionNumber(), savedVersion.getDocument().getId(), loggedUser.getId());

        return new CreateDocumentVersionResponse(
                savedVersion.getId(),
                document.getTitle(),
                loggedUser.getUsername(),
                savedVersion.getVersionNumber(),
                "Document version created successfully"
        );
    }

    @Transactional
    public ApproveDocumentVersionResponse approveVersion(
            ApproveDocumentVersionRequest request,
            String reviewerUsername
    ) {
        String errorMsg = "Cannot approve version -";

        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> {
                    logger.error("{} reviewer with username {} not found", errorMsg, reviewerUsername);
                    return new IllegalArgumentException("Reviewer not found");
                });

        DocumentVersion version = documentVersionRepository
                .findByIdAndDocumentId(request.getVersionId(), request.getDocumentId())
                .orElseThrow(() -> {
                    logger.error("{} version with id {} of document with id {} not found", errorMsg, request.getVersionId(), request.getDocumentId());
                    return new IllegalArgumentException("Version not found");
                });

        if (version.getStatus() != VersionStatus.DRAFT) {
            logger.error("{} version with id {} is not a draft version", errorMsg, version.getId());
            throw new IllegalArgumentException("Only draft versions can be approved");
        }

        Document document = version.getDocument();

        DocumentMember membership = documentMemberRepository.findByDocumentAndUser(document, reviewer)
                .orElseThrow(() -> {
                    logger.error("{} user with id {} does not have access to document with id {}", errorMsg, reviewer.getId(), document.getId());
                    return new IllegalArgumentException("User is not an authorized member for this action");
                });

        if (membership.getRole() != DocumentRole.REVIEWER) {
            logger.error("{} user with id {} is not a reviewer in document with id {}", errorMsg, reviewer.getId(), document.getId());
            throw new IllegalArgumentException("Only reviewers can approve versions");
        }

        if(request.getComment() != null){
            Comment comment = Comment.builder()
                    .version(version)
                    .user(reviewer)
                    .commentText(request.getComment())
                    .createdAt(LocalDateTime.now())
                    .build();
            commentRepository.save(comment);
        }

        version.setStatus(VersionStatus.APPROVED);
        version.setApprovedBy(reviewer);
        version.setApprovedAt(LocalDateTime.now());
        document.setActiveVersion(version);

        documentVersionRepository.save(version);
        documentRepository.save(document);

        logger.info("User with id {} successfully approved version with id {} of document with id {}", reviewer.getId(), version.getId(), document.getId());

        return new ApproveDocumentVersionResponse(
                document.getId(),
                document.getTitle(),
                version.getVersionNumber(),
                reviewer.getUsername(),
                version.getStatus().name(),
                request.getComment()
        );
    }

    @Transactional
    public RejectDocumentVersionResponse rejectVersion(
            RejectDocumentVersionRequest request,
            String reviewerUsername
    ) {
        String errorMsg = "Cannot reject version -";

        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> {
                    logger.error("{} reviewer with username {} not found", errorMsg, reviewerUsername);
                    return new IllegalArgumentException("Reviewer not found");
                });

        DocumentVersion version = documentVersionRepository
                .findByIdAndDocumentId(request.getVersionId(), request.getDocumentId())
                .orElseThrow(() -> {
                    logger.error("{} version with id {} of document with id {} not found", errorMsg, request.getVersionId(), request.getDocumentId());
                    return new IllegalArgumentException("Version not found");
                });

        if (version.getStatus() != VersionStatus.DRAFT) {
            logger.error("{} version with id {} is not a draft version", errorMsg, version.getId());
            throw new IllegalArgumentException("Only draft versions can be rejected");
        }

        Document document = version.getDocument();

        DocumentMember membership = documentMemberRepository.findByDocumentAndUser(document, reviewer)
                .orElseThrow(() -> {
                    logger.error("{} user with id {} does not have access to document with id {}", errorMsg, reviewer.getId(), document.getId());
                    return new IllegalArgumentException("User is not an authorized member for this action");
                });

        if (membership.getRole() != DocumentRole.REVIEWER) {
            logger.error("{} user with id {} is not a reviewer in document with id {}", errorMsg, reviewer.getId(), document.getId());
            throw new IllegalArgumentException("Only reviewers can reject versions");
        }

        Comment comment = Comment.builder()
                .version(version)
                .user(reviewer)
                .commentText(request.getReason())
                .createdAt(LocalDateTime.now())
                .build();

        version.setStatus(VersionStatus.REJECTED);
        version.setRejectedBy(reviewer);
        version.setRejectedAt(LocalDateTime.now());

        documentVersionRepository.save(version);
        commentRepository.save(comment);

        logger.info("User with id {} successfully rejected version with id {} of document with id {}", reviewer.getId(), version.getId(), document.getId());

        return new RejectDocumentVersionResponse(
                document.getId(),
                document.getTitle(),
                version.getVersionNumber(),
                reviewer.getUsername(),
                version.getStatus().name(),
                request.getReason()
        );
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionHistoryResponse> getVersionHistory(Long documentId) {
        return documentVersionRepository.findAllByDocumentIdOrderByVersionNumberDesc(documentId)
                .stream()
                .map(version -> new DocumentVersionHistoryResponse(
                        version.getId(),
                        version.getVersionNumber(),
                        version.getStatus().name(),
                        version.getCreatedBy() != null ? version.getCreatedBy().getUsername() : null,
                        version.getApprovedBy() != null ? version.getApprovedBy().getUsername() : null,
                        version.getRejectedBy() != null ? version.getRejectedBy().getUsername() : null,
                        version.getCreatedAt(),
                        version.getApprovedAt(),
                        version.getRejectedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentVersionDetailsResponse getActiveVersion(Long documentId, String username) {
        Document document = getDocumentAccessibleByLoggedUser(documentId, username);

        DocumentVersion activeVersion = document.getActiveVersion();

        if (activeVersion == null) {
            logger.error("Cannot access active version - document with id {} has no active version", documentId);
            throw new IllegalArgumentException("Document has no active version");
        }

        logger.info("User with username {} successfully accessed active version with id {} of document with id {}", username, documentId, activeVersion.getId());

        return mapToVersionDetailsResponse(activeVersion);
    }

    @Transactional(readOnly = true)
    public DocumentVersionDetailsResponse getParentVersion(Long documentId, String username) {
        Document document = getDocumentAccessibleByLoggedUser(documentId, username);

        DocumentVersion activeVersion = document.getActiveVersion();

        if (activeVersion == null) {
            logger.error("Cannot access parent version - document with id {} has no active version", documentId);
            throw new IllegalArgumentException("Document has no active version");
        }

        DocumentVersion parentVersion = activeVersion.getParentVersion();

        if (parentVersion == null) {
            logger.error("Cannot access parent version - active version with id {} of document with id {} has no parent version", activeVersion.getId(),documentId);
            throw new IllegalArgumentException("Active version has no parent version");
        }

        logger.info("User with username {} successfully accessed parent version of active version with id {} of document with id {}", username, documentId, activeVersion.getId());

        return mapToVersionDetailsResponse(parentVersion);
    }

    private Document getDocumentAccessibleByLoggedUser(Long documentId, String username) {
        User loggedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Cannot acquire access of user to document - logged user with username {} not found", username);
                    return new IllegalArgumentException("Logged user not found");
                });

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    logger.error("Cannot acquire access of user to document - document with id {} not found", documentId);
                    return new IllegalArgumentException("Document not found");
                });

        documentMemberRepository.findByDocumentAndUser(document, loggedUser)
                .orElseThrow(() -> {
                    logger.error("User with username {} does not have access to document with id {}", username, documentId);
                    return new IllegalArgumentException("You don't have access to the document");
                });

        return document;
    }

    private DocumentVersionDetailsResponse mapToVersionDetailsResponse(DocumentVersion version) {
        return new DocumentVersionDetailsResponse(
                version.getId(),
                version.getDocument().getId(),
                version.getDocument().getTitle(),
                version.getVersionNumber(),
                version.getStatus().name(),
                version.getCreatedBy() != null ? version.getCreatedBy().getUsername() : null,
                version.getOriginalFileName(),
                version.getContentType(),
                version.getFileSize(),
                version.getCreatedAt()
        );
    }


    @Transactional(readOnly = true)
    public DocumentFileResponse downloadVersionFile(Long versionId, Long documentId) {
        DocumentVersion version = documentVersionRepository
                .findByIdAndDocumentId(versionId, documentId)
                .orElseThrow(() -> {
                    logger.error("Cannot download version - version with id {} of document with id {} not found", versionId, documentId);
                    return new IllegalArgumentException("Version not found");
                });

        return documentFileStorageService.readFile(version);
    }



}