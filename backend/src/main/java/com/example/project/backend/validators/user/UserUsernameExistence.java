package com.example.project.backend.validators.user;

import com.example.project.backend.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UserUsernameExistence implements ConstraintValidator<ValidateUsernameExistence, String> {


    private final UserRepository userRepository;

    @Autowired
    public UserUsernameExistence(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void initialize(ValidateUsernameExistence constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
        return this.userRepository.findByUsername(username).isEmpty();
    }
}
