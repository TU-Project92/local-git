package com.example.project.backend.service;

import com.example.project.backend.dto.response.documentVersion.DocumentFileResponse;
import com.example.project.backend.model.entity.DocumentVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class DocumentFileStorageService {

    private final Path storageRoot;

    public DocumentFileStorageService(
            @Value("${app.storage.documents-dir:documents-storage}") String storageDirectory
    ) {
        this.storageRoot = Paths.get(storageDirectory).toAbsolutePath().normalize();
    }

    public StoredFileData saveFile(Long documentId, Integer versionNumber, MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename() != null
                    ? Paths.get(file.getOriginalFilename()).getFileName().toString()
                    : "uploaded-file";

            String safeFileName = UUID.randomUUID() + "_" + originalFileName;

            Path documentDirectory = storageRoot.resolve("document-" + documentId);
            Files.createDirectories(documentDirectory);

            Path targetPath = documentDirectory.resolve("version-" + versionNumber + "_" + safeFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFileData(
                    targetPath.toString(),
                    originalFileName,
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to store uploaded file", ex);
        }
    }

    public DocumentFileResponse readFile(DocumentVersion version) {
        try {
            Path path = Paths.get(version.getFilePath());
            byte[] content = Files.readAllBytes(path);

            String contentType = version.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = Files.probeContentType(path);
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            return new DocumentFileResponse(
                    version.getOriginalFileName(),
                    contentType,
                    content
            );
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read stored file", ex);
        }
    }

    public record StoredFileData(
            String filePath,
            String originalFileName,
            String contentType,
            Long fileSize
    ) {
    }
}