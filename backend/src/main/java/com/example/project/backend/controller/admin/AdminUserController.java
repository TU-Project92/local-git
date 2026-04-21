package com.example.project.backend.controller.admin;

import com.example.project.backend.dto.request.user.UserActivationRequest;
import com.example.project.backend.dto.request.user.UserDeactivationRequest;
import com.example.project.backend.dto.response.user.UserActivationResponse;
import com.example.project.backend.dto.response.user.UserDeactivationResponse;
import com.example.project.backend.dto.response.user.UserSearchResponse;
import com.example.project.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                userService.searchUsersForAdmin(authentication.getName(), search)
        );
    }

    @PatchMapping("/deactivate")
    public ResponseEntity<UserDeactivationResponse> deactivateUser(
            @RequestBody @Valid UserDeactivationRequest request,
            Authentication authentication
    ){
        return ResponseEntity.ok(
                userService.deactivateUser(request.getUserId(), authentication.getName())
        );
    }

    @PatchMapping("/activate")
    public ResponseEntity<UserActivationResponse> activateUser(
            @RequestBody @Valid UserActivationRequest request,
            Authentication authentication
    ){
        return ResponseEntity.ok(
                userService.activateUser(request.getUserId(), authentication.getName())
        );
    }
}