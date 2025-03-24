package app.kongkow.social.user.controller;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.social.user.dto.FollowDto;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
import app.kongkow.social.user.service.FollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<FollowDto.FollowResponse> followUser(
            @Valid @RequestBody FollowDto.FollowRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        FollowDto.FollowResponse response = followService.followUser(request.getUserId(), currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<FollowDto.FollowResponse> unfollowUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        FollowDto.FollowResponse response = followService.unfollowUser(userId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{userId}")
    public ResponseEntity<FollowDto.FollowResponse> getFollowStatus(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        FollowDto.FollowResponse response = followService.getFollowStatus(userId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<FollowDto.FollowerResponse>> getFollowers(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<FollowDto.FollowerResponse> followers = followService.getFollowers(userId, currentUser, pageable);
        return ResponseEntity.ok(followers);
    }
    
    @GetMapping("/{userId}/following")
    public ResponseEntity<Page<FollowDto.FollowingResponse>> getFollowing(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<FollowDto.FollowingResponse> following = followService.getFollowing(userId, currentUser, pageable);
        return ResponseEntity.ok(following);
    }
}