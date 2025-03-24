package app.kongkow.social.thread.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class TrendingDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HashtagResponse {
        private Long id;
        private String name;
        private long usageCount;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendingHashtagsResponse {
        private List<HashtagResponse> hashtags;
        private String timeframe;  // e.g., "24h", "week", "month"
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendingThreadsResponse {
        private List<ThreadDto.ThreadResponse> threads;
        private String timeframe;  // e.g., "24h", "week", "month"
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResponse {
        private List<ThreadDto.ThreadResponse> threads;
        private List<HashtagResponse> hashtags;
        private String query;
        private int totalResults;
    }
}