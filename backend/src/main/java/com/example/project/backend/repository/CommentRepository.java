package com.example.project.backend.repository;

import com.example.project.backend.model.entity.Comment;
import com.example.project.backend.model.entity.DocumentVersion;
import com.example.project.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByVersion(DocumentVersion version);
    List<Comment> findAllByUser(User user);
}
