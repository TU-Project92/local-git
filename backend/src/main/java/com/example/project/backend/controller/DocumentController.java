package com.example.project.backend.controller;

import com.example.project.backend.dto.request.document.CreateFirstDocumentRequest;
import com.example.project.backend.dto.response.document.CreateFirstDocumentResponse;
import com.example.project.backend.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/createFirst")
    public ResponseEntity<CreateFirstDocumentResponse> createDocument(
            @RequestBody @Valid CreateFirstDocumentRequest request,
            Authentication authentication
    ) {
        CreateFirstDocumentResponse response =
                documentService.createFirstDocument(request, authentication.getName());
        System.out.println(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}