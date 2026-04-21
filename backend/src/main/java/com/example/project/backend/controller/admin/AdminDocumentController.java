package com.example.project.backend.controller.admin;

import com.example.project.backend.dto.response.admin.AdminDocumentTableResponse;
import com.example.project.backend.dto.response.documentVersion.DeleteDocumentResponse;
import com.example.project.backend.dto.response.documentVersion.DeleteDocumentVersionResponse;
import com.example.project.backend.service.DocumentService;
import com.example.project.backend.service.DocumentVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<AdminDocumentTableResponse>> getAdminDocuments(
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                documentService.getAdminDocuments(authentication.getName(), search)
        );
    }


    @DeleteMapping("/{documentId}")
    public ResponseEntity<DeleteDocumentResponse> deleteDocument(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                documentService.deleteDocument(documentId, authentication.getName())
        );
    }
}