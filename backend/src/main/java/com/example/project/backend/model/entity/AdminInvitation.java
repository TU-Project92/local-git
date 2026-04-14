package com.example.project.backend.model.entity;

import com.example.project.backend.model.enums.InvitationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminInvitation extends BaseEntity{

    @ManyToOne
    private User sender;

    @ManyToOne
    private User recipient;

    @Enumerated (EnumType.STRING)
    private InvitationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}
