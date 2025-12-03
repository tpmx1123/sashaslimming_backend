package com.example.slimming.repository;

import com.example.slimming.entity.Blog;
import com.example.slimming.enums.BlogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByIsFeatured(Boolean isFeatured);
    List<Blog> findByCategory(String category);
    Optional<Blog> findById(Long id);
    List<Blog> findByStatus(BlogStatus status);
    Optional<Blog> findByTitleIgnoreCase(String title);
    Optional<Blog> findBySlug(String slug);
}
