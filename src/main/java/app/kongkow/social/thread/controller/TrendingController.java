package app.kongkow.social.thread.controller;
import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.social.thread.dto.TrendingDto;
import app.kongkow.social.thread.service.TrendingService;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trending")
@RequiredArgsConstructor
@Tag(name = "Trending", description = "APIs for trending and content discovery")
public class TrendingController {

    private final TrendingService trendingService;
    private final UserRepository userRepository;
    
    @Operation(summary = "Get trending hashtags", description = "Retrieve top trending hashtags based on timeframe")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trending hashtags retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = TrendingDto.TrendingHashtagsResponse.class)))
    })
    @GetMapping("/hashtags")
    public ResponseEntity<TrendingDto.TrendingHashtagsResponse> getTrendingHashtags(
            @Parameter(description = "Timeframe for trending calculation (24h, week, month)", example = "24h")
            @RequestParam(defaultValue = "24h") String timeframe,
            @PageableDefault(size = 10) Pageable pageable) {
        
        TrendingDto.TrendingHashtagsResponse response = trendingService.getTrendingHashtags(timeframe, pageable);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get trending threads", description = "Retrieve top trending threads based on timeframe")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trending threads retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = TrendingDto.TrendingThreadsResponse.class)))
    })
    @GetMapping("/threads")
    public ResponseEntity<TrendingDto.TrendingThreadsResponse> getTrendingThreads(
            @Parameter(description = "Timeframe for trending calculation (24h, week, month)", example = "24h")
            @RequestParam(defaultValue = "24h") String timeframe,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        TrendingDto.TrendingThreadsResponse response = trendingService.getTrendingThreads(timeframe, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get threads by hashtag", description = "Retrieve threads associated with a specific hashtag")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Threads retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = TrendingDto.TrendingThreadsResponse.class)))
    })
    @GetMapping("/hashtag/{hashtag}")
    public ResponseEntity<TrendingDto.TrendingThreadsResponse> getThreadsByHashtag(
            @Parameter(description = "Hashtag name (with or without #)", example = "programming")
            @PathVariable String hashtag,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        // Remove the # if it's present at the beginning
        if (hashtag.startsWith("#")) {
            hashtag = hashtag.substring(1);
        }
        
        TrendingDto.TrendingThreadsResponse response = trendingService.getThreadsByHashtag(hashtag, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Search threads and hashtags", description = "Search for threads and hashtags by query")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = TrendingDto.SearchResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<TrendingDto.SearchResponse> search(
            @Parameter(description = "Search query", example = "technology")
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        TrendingDto.SearchResponse response = trendingService.searchThreadsAndHashtags(query, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
}