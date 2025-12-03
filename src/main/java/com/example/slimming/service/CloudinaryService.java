package com.example.slimming.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:}") String apiSecret) {
        
        // Validate Cloudinary configuration
        if (cloudName == null || cloudName.isEmpty()) {
            throw new IllegalStateException("CLOUDINARY_CLOUD_NAME is not set in environment variables. Please add it to your .env file.");
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("CLOUDINARY_API_KEY is not set in environment variables. Please add it to your .env file.");
        }
        if (apiSecret == null || apiSecret.isEmpty()) {
            throw new IllegalStateException("CLOUDINARY_API_SECRET is not set in environment variables. Please add it to your .env file.");
        }
        
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        
        this.cloudinary = new Cloudinary(config);
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "blog-images"
                    )
            );
            
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }
}


