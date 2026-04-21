package com.example.project.backend.controller.admin;

import com.example.project.backend.dto.response.documentVersion.DeleteDocumentVersionResponse;
import com.example.project.backend.service.DocumentVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/documentVersions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDocumentVersionController {

    private final DocumentVersionService documentVersionService;

    @DeleteMapping("/{versionId}")
    public ResponseEntity<DeleteDocumentVersionResponse> deleteDocumentVersion(
            @PathVariable Long versionId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                documentVersionService.deleteDocumentVersion(versionId, authentication.getName())
        );
    }
}
