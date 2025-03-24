package app.kongkow.social.thread.controller;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.social.thread.dto.TrendingDto;
import app.kongkow.social.thread.service.TrendingService;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
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
public class TrendingController {

    private final TrendingService trendingService;
    private final UserRepository userRepository;
    
    @GetMapping("/hashtags")
    public ResponseEntity<TrendingDto.TrendingHashtagsResponse> getTrendingHashtags(
            @RequestParam(defaultValue = "24h") String timeframe,
            @PageableDefault(size = 10) Pageable pageable) {
        
        TrendingDto.TrendingHashtagsResponse response = trendingService.getTrendingHashtags(timeframe, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/threads")
    public ResponseEntity<TrendingDto.TrendingThreadsResponse> getTrendingThreads(
            @RequestParam(defaultValue = "24h") String timeframe,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        TrendingDto.TrendingThreadsResponse response = trendingService.getTrendingThreads(timeframe, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/hashtag/{hashtag}")
    public ResponseEntity<TrendingDto.TrendingThreadsResponse> getThreadsByHashtag(
            @PathVariable String hashtag,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
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
    
    @GetMapping("/search")
    public ResponseEntity<TrendingDto.SearchResponse> search(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        TrendingDto.SearchResponse response = trendingService.searchThreadsAndHashtags(query, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
}