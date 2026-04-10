package com.ratingapp.profile.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ratingapp.config.S3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarService {

    private final AmazonS3 s3Client;
    private final S3Config s3Config;

    public String uploadAvatar(MultipartFile file, UUID userId) {
        try {
            validateFile(file);
            
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String fileName = String.format("avatars/%s_%d%s", 
                userId.toString(), 
                System.currentTimeMillis(), 
                fileExtension);
            
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest request = new PutObjectRequest(
                    s3Config.getBucketName(),
                    fileName,
                    inputStream,
                    metadata
                );
                
                s3Client.putObject(request);
            }
            
            return String.format("%s/%s", s3Config.getPublicUrl(), fileName);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
    }
    
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }
        
        String key = avatarUrl.replace(s3Config.getPublicUrl() + "/", "");
        s3Client.deleteObject(s3Config.getBucketName(), key);
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }
        
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size must be less than 5 MB");
        }
        
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = getFileExtension(filename);
            if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
                throw new RuntimeException("Only JPG, PNG, GIF, WEBP formats are allowed");
            }
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}