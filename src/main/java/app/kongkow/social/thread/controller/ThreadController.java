package app.kongkow.social.thread.controller;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.common.payload.ApiResponse;
import app.kongkow.social.thread.dto.ThreadDto;
import app.kongkow.social.thread.service.ThreadService;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/threads")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Threads", description = "Thread management and interaction APIs")
public class ThreadController {

    private final ThreadService threadService;
    private final UserRepository userRepository;

    @Operation(summary = "Create a new thread", description = "Create a text-only thread (post)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thread created successfully", 
                     content = @Content(schema = @Schema(implementation = ThreadDto.ThreadResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ThreadDto.ThreadResponse> createThread(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ThreadDto.ThreadRequest request) {
        ThreadDto.ThreadResponse response = threadService.createThread(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Create a thread with media", description = "Create a thread with attached media files")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thread with media created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ThreadDto.ThreadResponse> createThreadWithMedia(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("content") String content,
            @RequestPart(value = "parentId", required = false) Long parentId,
            @RequestPart(value = "media", required = false) List<MultipartFile> media) throws IOException {
        
        ThreadDto.ThreadWithMediaRequest request = ThreadDto.ThreadWithMediaRequest.builder()
                .content(content)
                .parentId(parentId)
                .media(media)
                .build();
        
        ThreadDto.ThreadResponse response = threadService.createThreadWithMedia(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get a thread by ID", description = "Retrieve a specific thread by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thread retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Thread not found")
    })
    @GetMapping("/{threadId}")
    public ResponseEntity<ThreadDto.ThreadResponse> getThread(
            @PathVariable Long threadId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        ThreadDto.ThreadResponse response = threadService.getThread(threadId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get threads by user", description = "Retrieve all threads created by a specific user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Threads retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<ThreadDto.ThreadResponse>> getThreadsByUser(
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<ThreadDto.ThreadResponse> response = threadService.getThreadsByUser(username, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get replies to a thread", description = "Retrieve all replies to a specific thread")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Replies retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Thread not found")
    })
    @GetMapping("/{threadId}/replies")
    public ResponseEntity<Page<ThreadDto.ThreadResponse>> getReplies(
            @PathVariable Long threadId,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<ThreadDto.ThreadResponse> response = threadService.getReplies(threadId, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get personalized feed", description = "Retrieve a personalized feed of threads from followed users")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Feed retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/feed")
    public ResponseEntity<Page<ThreadDto.ThreadResponse>> getFeed(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Page<ThreadDto.ThreadResponse> response = threadService.getFeed(currentUser, pageable);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get public timeline", description = "Retrieve a public timeline of recent threads")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Public timeline retrieved successfully")
    })
    @GetMapping("/public")
    public ResponseEntity<Page<ThreadDto.ThreadResponse>> getPublicTimeline(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<ThreadDto.ThreadResponse> response = threadService.getPublicTimeline(pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update a thread", description = "Update the content of an existing thread")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thread updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the thread owner"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Thread not found")
    })
    @PutMapping("/{threadId}")
    public ResponseEntity<ThreadDto.ThreadResponse> updateThread(
            @PathVariable Long threadId,
            @Valid @RequestBody ThreadDto.ThreadRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ThreadDto.ThreadResponse response = threadService.updateThread(threadId, request, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Delete a thread", description = "Delete an existing thread")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thread deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the thread owner"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Thread not found")
    })
    @DeleteMapping("/{threadId}")
    public ResponseEntity<ApiResponse> deleteThread(
            @PathVariable Long threadId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        threadService.deleteThread(threadId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Thread deleted successfully"));
    }
    
    @Operation(summary = "Like a thread", description = "Like a specific thread")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thread liked successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Thread not found")
    })
    @PostMapping("/{threadId}/like")
    public ResponseEntity<ThreadDto.LikeResponse> likeThread(
            @PathVariable Long threadId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ThreadDto.LikeResponse response = threadService.likeThread(threadId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Unlike a thread", description = "Remove a like from a specific thread")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thread unliked successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Thread not found")
    })
    @DeleteMapping("/{threadId}/like")
    public ResponseEntity<ThreadDto.LikeResponse> unlikeThread(
            @PathVariable Long threadId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ThreadDto.LikeResponse response = threadService.unlikeThread(threadId, currentUser);
        return ResponseEntity.ok(response);
    }
}