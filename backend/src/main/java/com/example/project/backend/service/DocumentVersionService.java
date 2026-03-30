package com.example.project.backend.service;

import com.example.project.backend.dto.request.documentVersion.ApproveDocumentVersionRequest;
import com.example.project.backend.dto.request.documentVersion.CreateDocumentVersionRequest;
import com.example.project.backend.dto.response.documentVersion.CreateDocumentVersionResponse;
import com.example.project.backend.dto.response.documentVersion.ApproveDocumentVersionResponse;
import com.example.project.backend.dto.request.documentVersion.RejectDocumentVersionRequest;
import com.example.project.backend.dto.response.documentVersion.RejectDocumentVersionResponse;
import com.example.project.backend.dto.response.documentVersion.DocumentVersionHistoryResponse;
import com.example.project.backend.dto.request.documentVersion.DocumentVersionHistoryRequest;

import com.example.project.backend.model.entity.Document;
import com.example.project.backend.model.entity.DocumentMember;
import com.example.project.backend.model.entity.DocumentVersion;
import com.example.project.backend.model.entity.User;
import com.example.project.backend.model.enums.DocumentRole;
import com.example.project.backend.model.enums.VersionStatus;
import com.example.project.backend.repository.DocumentMemberRepository;
import com.example.project.backend.repository.DocumentRepository;
import com.example.project.backend.repository.DocumentVersionRepository;
import com.example.project.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentVersionService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentMemberRepository documentMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateDocumentVersionResponse createDocumentVersion(CreateDocumentVersionRequest request, String username){

        User loggedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Logged user not found"));

        User documentOwner = userRepository.findByUsername(request.getOwner())
                .orElseThrow(() -> new IllegalArgumentException("The owner of the document not found"));

        Document document = documentRepository.findByTitleAndCreatedBy(request.getTitle(), documentOwner)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        DocumentMember documentMember = documentMemberRepository.findByDocumentAndUser(document, loggedUser)
                .orElseThrow(() -> new IllegalArgumentException("You don't have access to the document"));

        if(documentMember.getRole() != DocumentRole.AUTHOR && documentMember.getRole() != DocumentRole.OWNER){
            throw new IllegalArgumentException("You don't have the rights to make changes to this document");
        }

        DocumentVersion newVersion = DocumentVersion.builder()
                .document(document)
                .versionNumber(document.getActiveVersion().getVersionNumber() + 1)
                .content(request.getContent())
                .status(VersionStatus.DRAFT)
                .createdBy(loggedUser)
                .parentVersion(document.getActiveVersion())
                .build();

        DocumentVersion savedVersion = documentVersionRepository.save(newVersion);
        document.setActiveVersion(savedVersion);
        //documentRepository.save(document);

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
            User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

            DocumentVersion version = documentVersionRepository
                .findByIdAndDocumentId(request.getVersionId(), request.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));

            Document document = version.getDocument();

            DocumentMember membership = documentMemberRepository.findByDocumentAndUser(document, reviewer)
                .orElseThrow(() -> new IllegalArgumentException("User is not an authorized member for this action"));

            if (membership.getRole() != DocumentRole.REVIEWER) {
            throw new IllegalArgumentException("Only reviewers can approve versions");
            }


        version.setStatus(VersionStatus.APPROVED);
        version.setApprovedBy(reviewer);
        version.setApprovedAt(LocalDateTime.now());
        document.setActiveVersion(version);

        documentVersionRepository. save(version);
        documentRepository.save(document);

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
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

        DocumentVersion version = documentVersionRepository
                .findByIdAndDocumentId(request.getVersionId(), request.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));

        Document document = version.getDocument();

        DocumentMember membership = documentMemberRepository.findByDocumentAndUser(document, reviewer)
                .orElseThrow(() -> new IllegalArgumentException("User is not an authorized member for this action"));

        if (membership.getRole() != DocumentRole.REVIEWER) {
            throw new IllegalArgumentException("Only reviewers can reject versions");
        }

        version.setStatus(VersionStatus.REJECTED);
        version.setRejectedBy(reviewer);
        version.setRejectedAt(LocalDateTime.now());

        documentVersionRepository.save(version);

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

}
