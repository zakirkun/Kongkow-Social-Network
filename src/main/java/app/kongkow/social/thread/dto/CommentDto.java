package app.kongkow.social.thread.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentRequest {
        @NotBlank(message = "Content is required")
        @Size(max = 500, message = "Content cannot exceed 500 characters")
        private String content;
        
        @NotNull(message = "Thread ID is required")
        private Long threadId;
        
        private Long parentId;  // Optional, for replies to comments
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentResponse {
        private Long id;
        private String content;
        private UserSummary user;
        private Long threadId;
        private Long parentId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int likeCount;
        private int replyCount;
        private boolean liked;  // Whether the current user has liked this comment
        private List<CommentResponse> recentReplies;  // Optional, for showing a few recent replies
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
    public static class CommentLikeRequest {
        @NotNull(message = "Comment ID is required")
        private Long commentId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentLikeResponse {
        private boolean liked;
        private int likeCount;
    }
}
