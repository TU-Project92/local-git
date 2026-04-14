package com.example.project.backend.service;

import com.example.project.backend.dto.request.admin.CreateAdminProfileRequest;
import com.example.project.backend.dto.request.admin.InviteAdminRequest;
import com.example.project.backend.dto.response.admin.AdminInvitationResponse;
import com.example.project.backend.dto.response.admin.CreateAdminProfileResponse;
import com.example.project.backend.model.entity.AdminInvitation;
import com.example.project.backend.model.entity.User;
import com.example.project.backend.model.enums.InvitationStatus;
import com.example.project.backend.model.enums.NotificationType;
import com.example.project.backend.model.enums.SystemRole;
import com.example.project.backend.repository.AdminInvitationRepository;
import com.example.project.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.project.backend.dto.response.invite.ActionResponse;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminInvitationService {

    private final AdminInvitationRepository adminInvitationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminInvitationResponse inviteToBecomeAdmin(String adminUsername, InviteAdminRequest request) {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if (admin.getSystemRole() != SystemRole.ADMIN) {
            throw new IllegalArgumentException("Only admins can send invitations.");
        }

        User recipient = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Target user not found."));

        if (admin.getId().equals(recipient.getId())) {
            throw new IllegalArgumentException(("You cannot invite yourself."));
        }
        if (adminInvitationRepository.existsByRecipientAndStatus(recipient, InvitationStatus.PENDING)) {
            throw new IllegalArgumentException("This user already has a pending admin invitation");
        }

        AdminInvitation invitation = AdminInvitation.builder()
                .sender(admin)
                .recipient(recipient)
                .status(InvitationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        AdminInvitation savedInvitation = adminInvitationRepository.save(invitation);

        notificationService.send(
                recipient,
                admin,
                admin.getUsername() + " invited you to become an admin",
                NotificationType.ADMIN_REQUEST
        );

        return new AdminInvitationResponse(
                savedInvitation.getId(),
                admin.getUsername(),
                recipient.getUsername(),
                savedInvitation.getStatus().name(),
                "Admin invitation sent successfully"
        );

    }
    @Transactional
    public ActionResponse acceptAdminInvitation(Long invitationId, String username) {
        User loggedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Logged user not found"));

        AdminInvitation invitation = adminInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Admin invitation not found"));

        if (!invitation.getRecipient().getId().equals(loggedUser.getId())) {
            throw new IllegalArgumentException("You cannot accept this admin invitation");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("This admin invitation is no longer pending");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setProcessedAt(LocalDateTime.now());

        notificationService.send(
                invitation.getSender(),
                loggedUser,
                loggedUser.getUsername() + " accepted your admin invitation",
                NotificationType.ADMIN_ACCEPTED
        );

        return new ActionResponse("Admin invitation accepted successfully");
    }

    @Transactional
    public ActionResponse rejectAdminInvitation(Long invitationId, String username) {
        User loggedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Logged user not found"));

        AdminInvitation invitation = adminInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Admin invitation not found"));

        if (!invitation.getRecipient().getId().equals(loggedUser.getId())) {
            throw new IllegalArgumentException("You cannot reject this admin invitation");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("This admin invitation is no longer pending");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitation.setProcessedAt(LocalDateTime.now());

        notificationService.send(
                invitation.getSender(),
                loggedUser,
                loggedUser.getUsername() + " rejected your admin invitation",
                NotificationType.ADMIN_REJECTED
        );

        return new ActionResponse("Admin invitation rejected successfully");
    }

    @Transactional
    public CreateAdminProfileResponse createAdminProfile(String adminUsername, CreateAdminProfileRequest request) {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (admin.getSystemRole() != SystemRole.ADMIN) {
            throw new IllegalArgumentException("Only admins can create admin profiles");
        }

        AdminInvitation invitation = adminInvitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new IllegalArgumentException("Admin invitation not found"));

        if (invitation.getStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("The admin invitation must be accepted first");
        }

        User baseUser = invitation.getRecipient();

        if (userRepository.existsByLinkedUserAndSystemRole(baseUser, SystemRole.ADMIN)) {
            throw new IllegalArgumentException("This user already has an admin profile");
        }

        if (userRepository.existsByUsername(request.getAdminUsername())) {
            throw new IllegalArgumentException("Admin username already exists");
        }

        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new IllegalArgumentException("Admin email already exists");
        }

        User adminProfile = User.builder()
                .username(request.getAdminUsername())
                .firstName(baseUser.getFirstName())
                .lastName(baseUser.getLastName())
                .email(request.getAdminEmail())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .enabled(true)
                .active(true)
                .systemRole(SystemRole.ADMIN)
                .linkedUser(baseUser)
                .myInfo(baseUser.getMyInfo())
                .build();

        User savedAdminProfile = userRepository.save(adminProfile);

        return new CreateAdminProfileResponse(
                savedAdminProfile.getId(),
                baseUser.getId(),
                savedAdminProfile.getUsername(),
                savedAdminProfile.getEmail(),
                "Admin profile created successfully"
        );
    }
}