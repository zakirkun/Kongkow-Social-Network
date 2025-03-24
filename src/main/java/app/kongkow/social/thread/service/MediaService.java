package app.kongkow.social.thread.service;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.social.thread.entity.Media;
import app.kongkow.social.thread.entity.Thread;
import app.kongkow.social.thread.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    
    private final String UPLOAD_DIR = "uploads/";
    
    @Transactional
    public List<Media> saveMedia(List<MultipartFile> files, Thread thread) throws IOException {
        List<Media> savedMedia = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return savedMedia;
        }
        
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            // Save the file
            Files.copy(file.getInputStream(), filePath);
            
            // Determine media type
            String mediaType = determineMediaType(fileExtension);
            
            // Create media entity
            Media media = Media.builder()
                    .mediaType(mediaType)
                    .mediaUrl("/uploads/" + uniqueFilename)
                    .mediaAlt(originalFilename)
                    .thread(thread)
                    .build();
            
            savedMedia.add(mediaRepository.save(media));
        }
        
        return savedMedia;
    }
    
    private String determineMediaType(String fileExtension) {
        fileExtension = fileExtension.toLowerCase();
        if (fileExtension.matches("\\.(jpg|jpeg|png|webp)$")) {
            return "IMAGE";
        } else if (fileExtension.matches("\\.(mp4|avi|mov|webm)$")) {
            return "VIDEO";
        } else if (fileExtension.equals(".gif")) {
            return "GIF";
        } else {
            return "OTHER";
        }
    }
    
    @Transactional(readOnly = true)
    public List<Media> getMediaByThread(Thread thread) {
        return mediaRepository.findByThreadOrderByCreatedAtAsc(thread);
    }
    
    @Transactional
    public void deleteMedia(Long mediaId, Thread thread) {
        Media media = mediaRepository.findByIdAndThread(mediaId, thread);
        if (media == null) {
            throw new ResourceNotFoundException("Media not found");
        }
        
        // Delete file from filesystem
        try {
            String filePath = media.getMediaUrl().replace("/uploads/", "");
            Path path = Paths.get(UPLOAD_DIR + filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log error but continue with database deletion
        }
        
        mediaRepository.delete(media);
    }
    
    @Transactional
    public void deleteAllThreadMedia(Thread thread) {
        List<Media> mediaList = mediaRepository.findByThreadOrderByCreatedAtAsc(thread);
        
        // Delete files from filesystem
        for (Media media : mediaList) {
            try {
                String filePath = media.getMediaUrl().replace("/uploads/", "");
                Path path = Paths.get(UPLOAD_DIR + filePath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // Log error but continue with database deletion
            }
        }
        
        mediaRepository.deleteByThread(thread);
    }
}