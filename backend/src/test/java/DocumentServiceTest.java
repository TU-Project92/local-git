import com.example.project.backend.dto.response.document.CreateFirstDocumentResponse;
import com.example.project.backend.dto.response.document.DocumentListResponse;
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
import com.example.project.backend.service.DocumentFileStorageService;
import com.example.project.backend.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private DocumentMemberRepository documentMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentFileStorageService documentFileStorageService;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void shouldCreateFirstDocumentSuccessfully() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "plan.pdf",
                "application/pdf",
                "dummy-content".getBytes()
        );

        User loggedUser = User.builder()
                .username("ivan123")
                .email("ivan@example.com")
                .build();

        Document savedDocument = Document.builder()
                .title("Project Plan")
                .description("Initial project document")
                .createdBy(loggedUser)
                .build();
        savedDocument.setId(10L);

        DocumentFileStorageService.StoredFileData storedFile =
                new DocumentFileStorageService.StoredFileData(
                        "/uploads/documents/10/v1-plan.pdf",
                        "plan.pdf",
                        "application/pdf",
                        123L
                );

        DocumentVersion savedVersion = DocumentVersion.builder()
                .document(savedDocument)
                .versionNumber(1)
                .filePath("/uploads/documents/10/v1-plan.pdf")
                .originalFileName("plan.pdf")
                .contentType("application/pdf")
                .fileSize(123L)
                .status(VersionStatus.DRAFT)
                .createdBy(loggedUser)
                .build();
        savedVersion.setId(100L);

        when(userRepository.findByUsername("ivan123")).thenReturn(Optional.of(loggedUser));
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        when(documentFileStorageService.saveFile(10L, 1, file)).thenReturn(storedFile);
        when(documentVersionRepository.save(any(DocumentVersion.class))).thenReturn(savedVersion);

        CreateFirstDocumentResponse response = documentService.createFirstDocument(
                "Project Plan",
                "Initial project document",
                file,
                "ivan123"
        );

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Project Plan", response.getTitle());
        assertEquals("Initial project document", response.getDescription());
        assertEquals("ivan123", response.getCreatedByUsername());
        assertEquals(DocumentRole.OWNER, response.getRole());
        assertEquals("Document created successfully", response.getMessage());

        verify(userRepository).findByUsername("ivan123");
        verify(documentRepository, atLeastOnce()).save(any(Document.class));
        verify(documentMemberRepository).save(any(DocumentMember.class));
        verify(documentFileStorageService).saveFile(10L, 1, file);
        verify(documentVersionRepository).save(any(DocumentVersion.class));
    }

    @Test
    void shouldThrowWhenFileIsMissingWhileCreatingFirstDocument() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.createFirstDocument(
                        "Project Plan",
                        "Initial project document",
                        null,
                        "ivan123"
                )
        );

        assertEquals("File is required", exception.getMessage());

        verify(userRepository, never()).findByUsername(anyString());
        verify(documentRepository, never()).save(any(Document.class));
        verify(documentMemberRepository, never()).save(any(DocumentMember.class));
        verify(documentVersionRepository, never()).save(any(DocumentVersion.class));
        verify(documentFileStorageService, never()).saveFile(anyLong(), anyInt(), any());
    }

    @Test
    void shouldReturnLoggedUserDocumentsSuccessfully() {
        User creator = User.builder()
                .username("ownerUser")
                .email("owner@example.com")
                .build();

        DocumentVersion activeVersion = DocumentVersion.builder()
                .versionNumber(2)
                .filePath("/uploads/documents/20/v2-spec.pdf")
                .originalFileName("spec.pdf")
                .contentType("application/pdf")
                .fileSize(456L)
                .status(VersionStatus.APPROVED)
                .createdBy(creator)
                .build();

        Document document = Document.builder()
                .title("Project Plan")
                .description("Project description")
                .createdBy(creator)
                .activeVersion(activeVersion)
                .build();
        document.setId(20L);

        DocumentMember membership = DocumentMember.builder()
                .document(document)
                .role(DocumentRole.AUTHOR)
                .build();

        when(documentMemberRepository.findMyDocumentsByUsernameAndSearch("ivan123", "Project"))
                .thenReturn(List.of(membership));

        List<DocumentListResponse> response = documentService.getLoggedUserDocuments("ivan123", "Project");

        assertEquals(1, response.size());
        assertEquals(20L, response.get(0).getId());
        assertEquals("Project Plan", response.get(0).getTitle());
        assertEquals("Project description", response.get(0).getDescription());
        assertEquals("AUTHOR", response.get(0).getRole());
        assertEquals("ownerUser", response.get(0).getCreatedBy());
        assertEquals(2, response.get(0).getActiveVersionNumber());
        assertEquals("spec.pdf", response.get(0).getOriginalFileName());
        assertEquals("application/pdf", response.get(0).getContentType());
        assertEquals(456L, response.get(0).getFileSize());

        verify(documentMemberRepository).findMyDocumentsByUsernameAndSearch("ivan123", "Project");
    }

    @Test
    void shouldReturnEmptyListWhenNoDocumentsMatchSearch() {
        when(documentMemberRepository.findMyDocumentsByUsernameAndSearch("ivan123", "missing"))
                .thenReturn(List.of());

        List<DocumentListResponse> response = documentService.getLoggedUserDocuments("ivan123", "missing");

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(documentMemberRepository).findMyDocumentsByUsernameAndSearch("ivan123", "missing");
    }

}