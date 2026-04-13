package com.example.project.backend.service;
import com.example.project.backend.dto.response.user.UserActivationResponse;
import com.example.project.backend.dto.response.user.UserDeactivationResponse;
import com.example.project.backend.model.entity.User;
import com.example.project.backend.model.enums.SystemRole;
import com.example.project.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public UserDeactivationResponse deactivateUser(Long userId, String adminUsername){
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if(admin.getSystemRole() != SystemRole.ADMIN){
            throw new IllegalArgumentException("Only admins can deactivate users.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setActive(false);

        return new UserDeactivationResponse(
                user. getId(),
                user.getUsername(),
                user.isActive(),
                "User account is deactivated successfully. "
        );
    }

    @Transactional
    public UserActivationResponse activateUser(Long userId, String adminUsername){
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(()-> new IllegalArgumentException("Admin not found."));

        if(admin.getSystemRole() !=SystemRole.ADMIN){
            throw new IllegalArgumentException(" Only admins can activate users.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("User not found."));

        user.setActive(true);

        return new UserActivationResponse(
                user.getId(),
                user.getUsername(),
                user.isActive(),
                "User account is activated successfully."
        );
        }
    }


