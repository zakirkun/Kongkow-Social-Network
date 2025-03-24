package app.kongkow.social.user.controller;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.social.user.dto.FollowDto;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
import app.kongkow.social.user.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Follows", description = "User follow/unfollow and follower management APIs")
public class FollowController {

    private final FollowService followService;
    private final UserRepository userRepository;

    @Operation(summary = "Follow a user", description = "Follow another user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User followed successfully", 
                     content = @Content(schema = @Schema(implementation = FollowDto.FollowResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or trying to follow yourself"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping
    public ResponseEntity<FollowDto.FollowResponse> followUser(
            @Valid @RequestBody FollowDto.FollowRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        FollowDto.FollowResponse response = followService.followUser(request.getUserId(), currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Unfollow a user", description = "Unfollow a user that you are currently following")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User unfollowed successfully", 
                     content = @Content(schema = @Schema(implementation = FollowDto.FollowResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<FollowDto.FollowResponse> unfollowUser(
            @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        FollowDto.FollowResponse response = followService.unfollowUser(userId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get follow status", description = "Check if you are following a specific user and get follower counts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Follow status retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = FollowDto.FollowResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/status/{userId}")
    public ResponseEntity<FollowDto.FollowResponse> getFollowStatus(
            @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        FollowDto.FollowResponse response = followService.getFollowStatus(userId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get followers", description = "Get a list of users who follow a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Followers retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<FollowDto.FollowerResponse>> getFollowers(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<FollowDto.FollowerResponse> followers = followService.getFollowers(userId, currentUser, pageable);
        return ResponseEntity.ok(followers);
    }
    
    @Operation(summary = "Get following", description = "Get a list of users that a specific user follows")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Following list retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/following")
    public ResponseEntity<Page<FollowDto.FollowingResponse>> getFollowing(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<FollowDto.FollowingResponse> following = followService.getFollowing(userId, currentUser, pageable);
        return ResponseEntity.ok(following);
    }
}