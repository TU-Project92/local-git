package com.example.project.backend.controller;

import com.example.project.backend.dto.request.documentVersion.CreateDocumentVersionRequest;
import com.example.project.backend.dto.response.documentVersion.CreateDocumentVersionResponse;
import com.example.project.backend.dto.response.documentVersion.ApproveDocumentVersionResponse;
import com.example.project.backend.dto.response.documentVersion.DocumentVersionHistoryResponse;
import com.example.project.backend.dto.request.documentVersion.DocumentVersionHistoryRequest;
import com.example.project.backend.dto.request.documentVersion.ApproveDocumentVersionRequest;
import com.example.project.backend.dto.request.documentVersion.RejectDocumentVersionRequest;
import com.example.project.backend.dto.response.documentVersion.RejectDocumentVersionResponse;
import com.example.project.backend.model.entity.DocumentVersion;
import com.example.project.backend.service.DocumentVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documentVersions")
@RequiredArgsConstructor
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    @PostMapping("/createNew")
    public ResponseEntity<CreateDocumentVersionResponse> createVersion(
            @RequestBody @Valid CreateDocumentVersionRequest request,
            Authentication authentication
            ) {
        CreateDocumentVersionResponse response = documentVersionService.createDocumentVersion(request, authentication.getName());
        System.out.println(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PatchMapping("/approve")
    public ResponseEntity<ApproveDocumentVersionResponse> approveVersion(
            @RequestBody @Valid ApproveDocumentVersionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(documentVersionService.approveVersion(request, authentication.getName()));
    }

    @PatchMapping("/reject")
    public ResponseEntity<RejectDocumentVersionResponse> rejectVersion(
            @RequestBody @Valid RejectDocumentVersionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(documentVersionService.rejectVersion(request, authentication.getName()));
    }

    @PostMapping("/history")
    public ResponseEntity<List<DocumentVersionHistoryResponse>> getVersionHistory(
            @RequestBody @Valid DocumentVersionHistoryRequest request
    ) {
        return ResponseEntity.ok(documentVersionService.getVersionHistory(request.getDocumentId()));
    }

}
