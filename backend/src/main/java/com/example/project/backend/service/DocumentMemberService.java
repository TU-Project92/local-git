package com.example.project.backend.service;

import com.example.project.backend.dto.request.documentMember.CreateDocumentMemberRequest;
import com.example.project.backend.dto.request.documentMember.DeleteDocumentMemberRequest;
import com.example.project.backend.dto.response.documentMember.CreateDocumentMemberResponse;
import com.example.project.backend.dto.response.documentMember.DeleteDocumentMemberResponse;
import com.example.project.backend.dto.response.documentMember.SharedUserResponse;
import com.example.project.backend.model.entity.Document;
import com.example.project.backend.model.entity.DocumentMember;
import com.example.project.backend.model.entity.User;
import com.example.project.backend.model.enums.DocumentRole;
import com.example.project.backend.model.enums.SystemRole;
import com.example.project.backend.repository.DocumentMemberRepository;
import com.example.project.backend.repository.DocumentRepository;
import com.example.project.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentMemberService {

    private final DocumentRepository documentRepository;
    private final DocumentMemberRepository documentMemberRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(DocumentMemberService.class);


    @Transactional
    public CreateDocumentMemberResponse createDocumentMember(CreateDocumentMemberRequest request, String loggedUsername) {

        String errorMsg = "Cannot create document member -";

        User loggedUser = userRepository.findByUsername(loggedUsername)
                .orElseThrow(() -> {
                    logger.error("{} no logged user with username {} found", errorMsg, loggedUsername);
                    return new IllegalArgumentException("No logged user found");
                });

        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> {
                    logger.error("{} no document found with id {}", errorMsg, request.getDocumentId());
                    return new IllegalArgumentException("No document found");
                });

        User targetUser = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("{} no user found with username {}", errorMsg, request.getUsername());
                    return new IllegalArgumentException("User not found");
                });

        DocumentMember loggedMember = documentMemberRepository.findByDocumentAndUser(document, loggedUser)
                .orElseThrow(() -> {
                    logger.error("{} user with username {} does not have access to document with id {}", errorMsg, loggedUsername, document.getId());
                    return new IllegalArgumentException("You don't have access to this document");
                });

        if (loggedMember.getRole() != DocumentRole.OWNER) {
            logger.error("{} user with username {} is not the owner of document with id {}", errorMsg, loggedUsername, document.getId());
            throw new IllegalArgumentException("Only the owner can add members to this document");
        }

        if (loggedUser.getUsername().equalsIgnoreCase(targetUser.getUsername())) {
            logger.error("{} user with username {} tried to add themself again {} to document with id", errorMsg, loggedUsername, document.getId());
            throw new IllegalArgumentException("You cannot add yourself again");
        }

        DocumentRole role;
        try {
            role = DocumentRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            logger.error("{} invalid document role", errorMsg);
            throw new IllegalArgumentException("Invalid document role");
        }

        DocumentMember newMember = addUserToDocument(document, targetUser, role, loggedUser);

        logger.info("User with username {} successfully added user with username {} as {} in document with id {}", loggedUsername, targetUser.getUsername(), newMember.getRole().toString().toLowerCase(), document.getId());

        return new CreateDocumentMemberResponse(
                newMember.getId(),
                newMember.getRole(),
                newMember.getUser().getUsername(),
                newMember.getDocument().getTitle(),
                "User added successfully"
        );
    }

    @Transactional
    public DeleteDocumentMemberResponse deleteDocumentMember(DeleteDocumentMemberRequest request, String username){

        String errorMsg = "Cannot delete document member -";

        User loggedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("{} logged user with username not found", errorMsg, username);
                    return new IllegalArgumentException("No logged user found");
                });

        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> {
                    logger.error("{} no document found with id {}", errorMsg, request.getDocumentId());
                    return new IllegalArgumentException("No document found");
                });

        DocumentMember loggedMember = null;
        if(loggedUser.getSystemRole() != SystemRole.ADMIN){
            loggedMember = documentMemberRepository.findByDocumentAndUser(document, loggedUser)
                    .orElseThrow(() -> {
                        logger.error("{} user with username {} does not have access to document with id {}", errorMsg, username, document.getId());
                        return new IllegalArgumentException("You don't have access to this document");
                    });
        }

        if(loggedUser.getSystemRole() != SystemRole.ADMIN && loggedMember.getRole() != DocumentRole.OWNER){
            logger.error("{} user with username {} is not the owner of document with id {}", errorMsg, username, document.getId());
            throw new IllegalArgumentException("You don't have the rights to change roles for this document");
        }

        User member = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("{} user with username {} not found", errorMsg, request.getUsername());
                    return new IllegalArgumentException("User not found");
                });

        DocumentMember documentMember = documentMemberRepository.findByDocumentAndUser(document, member)
                .orElseThrow(() -> {
                    logger.error("{} user with username {} has no role in document with id {}", errorMsg, member.getUsername(), document.getId());
                    return new IllegalArgumentException("The user has no role in the document");
                });

        if(loggedMember == documentMember){
            logger.error("{} user with username {} tried to remove themself from document with id {}", errorMsg, username, document.getId());
            throw new IllegalArgumentException("You can't remove yourself from the document");
        }

        DeleteDocumentMemberResponse response = new DeleteDocumentMemberResponse(
                member.getUsername(),
                documentMember.getRole().toString(),
                document.getId(),
                "You removed the user successfully"
        );

        documentMemberRepository.delete(documentMember);

        logger.info("User with username {} successfully removed user with username {} from document with id {}", username, member.getUsername(), document.getId());

        return response;
    }

    @Transactional(readOnly = true)
    public List<SharedUserResponse> getSharedUsers(String username) {
        List<User> users = documentMemberRepository.findDistinctSharedUsersByUsername(username);

        logger.info("User with username {} successfully accessed their shared users", username);

        return users.stream()
                .map(user -> new SharedUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail()
                ))
                .toList();
    }

    @Transactional
    public DocumentMember addUserToDocument(
            Document document,
            User user,
            DocumentRole role,
            User addedBy
    ) {

        if (documentMemberRepository.findByDocumentAndUser(document, user).isPresent()) {
            logger.error("Cannot add user to document - user with id {} is already a member of document with id {}", user.getId(), document.getId());
            throw new IllegalArgumentException("User is already a member of this document");
        }

        DocumentMember newMember = DocumentMember.builder()
                .document(document)
                .user(user)
                .role(role)
                .addedBy(addedBy)
                .build();

        return documentMemberRepository.save(newMember);
    }
}