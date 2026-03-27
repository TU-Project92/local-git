package com.example.project.backend.repository;


import com.example.project.backend.model.entity.DocumentMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentMemberRepository extends JpaRepository<DocumentMember, Long> {
}
