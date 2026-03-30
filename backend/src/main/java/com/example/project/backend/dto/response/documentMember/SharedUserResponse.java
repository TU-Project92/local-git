package com.example.project.backend.dto.response.documentMember;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SharedUserResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
}
