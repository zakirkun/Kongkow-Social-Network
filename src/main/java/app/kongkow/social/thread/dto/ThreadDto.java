package app.kongkow.social.thread.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ThreadDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadRequest {
        @NotBlank(message = "Content is required")
        @Size(max = 1000, message = "Content cannot exceed 1000 characters")
        private String content;
        
        private Long parentId;  // Optional, for replies
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadWithMediaRequest {
        @NotBlank(message = "Content is required")
        @Size(max = 1000, message = "Content cannot exceed 1000 characters")
        private String content;
        
        private Long parentId;  // Optional, for replies
        
        private List<MultipartFile> media = new ArrayList<>();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadResponse {
        private Long id;
        private String content;
        private UserSummary user;
        private Long parentId;
        private List<MediaResponse> media;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int likeCount;
        private int replyCount;
        private boolean liked;  // Whether the current user has liked this thread
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String username;
        private String fullName;
        private String profilePicture;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaResponse {
        private Long id;
        private String mediaType;
        private String mediaUrl;
        private String mediaAlt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeRequest {
        @NotNull(message = "Thread ID is required")
        private Long threadId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeResponse {
        private boolean liked;
        private int likeCount;
    }
}