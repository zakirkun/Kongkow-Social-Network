package app.kongkow.social.thread.controller;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.common.payload.ApiResponse;
import app.kongkow.social.thread.dto.ThreadDto;
import app.kongkow.social.thread.service.ThreadService;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
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
public class ThreadController {

    private final ThreadService threadService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ThreadDto.ThreadResponse> createThread(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ThreadDto.ThreadRequest request) {
        ThreadDto.ThreadResponse response = threadService.createThread(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ThreadDto.ThreadResponse> createThreadWithMedia(
            @AuthenticationPrincipal UserDetails userDetails,
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
    
    @GetMapping("/{threadId}")
    public ResponseEntity<ThreadDto.ThreadResponse> getThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        ThreadDto.ThreadResponse response = threadService.getThread(threadId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<ThreadDto.ThreadResponse>> getThreadsByUser(
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<ThreadDto.ThreadResponse> response = threadService.getThreadsByUser(username, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{threadId}/replies")
    public ResponseEntity<Page<ThreadDto.ThreadResponse>> getReplies(
            @PathVariable Long threadId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<ThreadDto.ThreadResponse> response = threadService.getReplies(threadId, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/feed")
    public ResponseEntity<Page<ThreadDto.ThreadResponse>> getFeed(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Page<ThreadDto.ThreadResponse> response = threadService.getFeed(currentUser, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/public")
    public ResponseEntity<Page<ThreadDto.ThreadResponse>> getPublicTimeline(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<ThreadDto.ThreadResponse> response = threadService.getPublicTimeline(pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{threadId}")
    public ResponseEntity<ThreadDto.ThreadResponse> updateThread(
            @PathVariable Long threadId,
            @Valid @RequestBody ThreadDto.ThreadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ThreadDto.ThreadResponse response = threadService.updateThread(threadId, request, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{threadId}")
    public ResponseEntity<ApiResponse> deleteThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        threadService.deleteThread(threadId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Thread deleted successfully"));
    }
    
    @PostMapping("/{threadId}/like")
    public ResponseEntity<ThreadDto.LikeResponse> likeThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ThreadDto.LikeResponse response = threadService.likeThread(threadId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{threadId}/like")
    public ResponseEntity<ThreadDto.LikeResponse> unlikeThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ThreadDto.LikeResponse response = threadService.unlikeThread(threadId, currentUser);
        return ResponseEntity.ok(response);
    }
}

