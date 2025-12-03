package com.example.slimming.controller;

import com.example.slimming.entity.Blog;
import com.example.slimming.enums.BlogStatus;
import com.example.slimming.repository.BlogRepository;
import com.example.slimming.service.EmailService;
import com.example.slimming.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.text.Normalizer;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/blogs")
@CrossOrigin(origins = {"https://lumiereluxe.in", "https://www.lumiereluxe.in", "http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
public class BlogController {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/public/all")
    public ResponseEntity<List<Blog>> getAllBlogs() {
        // Return all published blogs
        List<Blog> publishedBlogs = blogRepository.findByStatus(BlogStatus.PUBLISHED);
        // Ensure all blogs have slugs (generate if missing)
        for (Blog blog : publishedBlogs) {
            if (blog.getSlug() == null || blog.getSlug().isEmpty()) {
                String generatedSlug = generateSlug(blog.getTitle());
                blog.setSlug(generatedSlug);
                // Save the blog with the generated slug
                blogRepository.save(blog);
            }
        }
        return ResponseEntity.ok(publishedBlogs);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable String id) {
        // Decode URL encoding (Spring usually does this, but be safe)
        String decodedId = id;
        try {
            decodedId = URLDecoder.decode(id, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // If decoding fails, use original id
            decodedId = id;
        }
        
        // Try to parse as Long (for backward compatibility)
        try {
            Long blogId = Long.parseLong(decodedId);
            Optional<Blog> blog = blogRepository.findById(blogId);
            if (blog.isPresent() && blog.get().getStatus() == BlogStatus.PUBLISHED) {
                return ResponseEntity.ok(blog.get());
            }
        } catch (NumberFormatException e) {
            // Not a number, treat as slug
            // First try exact match with stored slug (case-insensitive)
            Optional<Blog> blog = blogRepository.findBySlug(decodedId);
            if (blog.isPresent() && blog.get().getStatus() == BlogStatus.PUBLISHED) {
                return ResponseEntity.ok(blog.get());
            }
            
            // Also try case-insensitive match
            List<Blog> allBlogs = blogRepository.findAll();
            for (Blog b : allBlogs) {
                if (b.getSlug() != null && b.getSlug().equalsIgnoreCase(decodedId) 
                    && b.getStatus() == BlogStatus.PUBLISHED) {
                    return ResponseEntity.ok(b);
                }
            }
            
            // Fallback: try to find by matching generated slug from title
            // Get all published blogs and check if any title generates to this slug
            List<Blog> allPublished = blogRepository.findByStatus(BlogStatus.PUBLISHED);
            for (Blog publishedBlog : allPublished) {
                // Generate slug from title
                String generatedSlug = generateSlug(publishedBlog.getTitle());
                if (generatedSlug.equalsIgnoreCase(decodedId)) {
                    return ResponseEntity.ok(publishedBlog);
                }
                // Also check the stored slug if it exists (case-insensitive)
                if (publishedBlog.getSlug() != null && publishedBlog.getSlug().equalsIgnoreCase(decodedId)) {
                    return ResponseEntity.ok(publishedBlog);
                }
            }
            
            // Last fallback: try to find by title (simple hyphen replacement)
            String title = decodedId.replace("-", " ");
            Optional<Blog> blogByTitle = blogRepository.findByTitleIgnoreCase(title);
            if (blogByTitle.isPresent() && blogByTitle.get().getStatus() == BlogStatus.PUBLISHED) {
                return ResponseEntity.ok(blogByTitle.get());
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/public/featured")
    public ResponseEntity<List<Blog>> getFeaturedBlogs() {
        // Return published featured blogs
        List<Blog> allFeatured = blogRepository.findByIsFeatured(true);
        List<Blog> publishedFeatured = allFeatured.stream()
            .filter(blog -> blog.getStatus() == BlogStatus.PUBLISHED)
            .toList();
        return ResponseEntity.ok(publishedFeatured);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Blog>> getAllBlogsForAdmin() {
        List<Blog> blogs = blogRepository.findAll();
        return ResponseEntity.ok(blogs);
    }

    @PostMapping("/admin")
    public ResponseEntity<Blog> createBlog(@RequestBody Blog blog) {
        // Generate slug if not provided
        if (blog.getSlug() == null || blog.getSlug().isEmpty()) {
            blog.setSlug(generateSlug(blog.getTitle()));
        } else {
            // Ensure slug is URL-friendly
            blog.setSlug(generateSlug(blog.getSlug()));
        }
        
        // Ensure slug is unique
        String baseSlug = blog.getSlug();
        String uniqueSlug = baseSlug;
        int counter = 1;
        while (blogRepository.findBySlug(uniqueSlug).isPresent()) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        blog.setSlug(uniqueSlug);
        
        Blog savedBlog = blogRepository.save(blog);
        
        // Send email notification if blog is published
        if (savedBlog.getStatus() == BlogStatus.PUBLISHED) {
            try {
                emailService.sendBlogNotification(savedBlog);
            } catch (Exception e) {
                System.err.println("Failed to send blog notification: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(savedBlog);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Blog> updateBlog(@PathVariable Long id, @RequestBody Blog blogDetails) {
        Optional<Blog> optionalBlog = blogRepository.findById(id);
        if (optionalBlog.isPresent()) {
            Blog blog = optionalBlog.get();
            BlogStatus previousStatus = blog.getStatus();
            
            blog.setTitle(blogDetails.getTitle());
            blog.setExcerpt(blogDetails.getExcerpt());
            blog.setImage(blogDetails.getImage());
            blog.setCategory(blogDetails.getCategory());
            blog.setMetaDescription(blogDetails.getMetaDescription());
            blog.setTags(blogDetails.getTags());
            
            // Update slug if title changed or slug is empty
            if (blogDetails.getSlug() != null && !blogDetails.getSlug().isEmpty()) {
                String newSlug = generateSlug(blogDetails.getSlug());
                // Check if slug is unique (excluding current blog)
                Optional<Blog> existingBlog = blogRepository.findBySlug(newSlug);
                if (existingBlog.isPresent() && !existingBlog.get().getId().equals(blog.getId())) {
                    // Slug exists for another blog, make it unique
                    String baseSlug = newSlug;
                    int counter = 1;
                    while (blogRepository.findBySlug(baseSlug + "-" + counter).isPresent() && 
                           !blogRepository.findBySlug(baseSlug + "-" + counter).get().getId().equals(blog.getId())) {
                        counter++;
                    }
                    blog.setSlug(baseSlug + "-" + counter);
                } else {
                    blog.setSlug(newSlug);
                }
            } else if (!blog.getTitle().equals(blogDetails.getTitle())) {
                // Title changed, regenerate slug
                blog.setSlug(generateSlug(blogDetails.getTitle()));
            }
            
            blog.setContent(blogDetails.getContent());
            blog.setIsFeatured(blogDetails.getIsFeatured());
            blog.setStatus(blogDetails.getStatus());
            Blog updatedBlog = blogRepository.save(blog);
            
            // Send email notification if status changed from non-published to published
            if (previousStatus != BlogStatus.PUBLISHED && updatedBlog.getStatus() == BlogStatus.PUBLISHED) {
                try {
                    emailService.sendBlogNotification(updatedBlog);
                } catch (Exception e) {
                    System.err.println("Failed to send blog notification: " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(updatedBlog);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Long id) {
        if (blogRepository.existsById(id)) {
            blogRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/admin/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            String imageUrl = cloudinaryService.uploadImage(file);
            response.put("url", imageUrl);
            response.put("success", "true");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Helper method to generate URL-friendly slug from title
    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }
        
        // Convert to lowercase
        String slug = title.toLowerCase();
        
        // Remove accents/diacritics
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        slug = slug.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        
        // Replace spaces and special characters with hyphens
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("-+", "-");
        slug = slug.replaceAll("^-|-$", "");
        
        return slug;
    }
}
