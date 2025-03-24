package app.kongkow.social.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class FollowDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowRequest {
        @NotNull(message = "User ID is required")
        private Long userId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowResponse {
        private boolean following;
        private int followerCount;
        private int followingCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowerResponse {
        private Long id;
        private String username;
        private String fullName;
        private String profilePicture;
        private LocalDateTime followedAt;
        private boolean following;  // Whether the current user follows this follower
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowingResponse {
        private Long id;
        private String username;
        private String fullName;
        private String profilePicture;
        private LocalDateTime followedAt;
        private boolean following;  // Should always be true in this context
    }
}