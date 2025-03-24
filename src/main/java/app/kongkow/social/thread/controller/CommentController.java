package app.kongkow.social.thread.controller;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.common.payload.ApiResponse;
import app.kongkow.social.thread.dto.CommentDto;
import app.kongkow.social.thread.service.CommentService;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
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
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<CommentDto.CommentResponse> createComment(
            @Valid @RequestBody CommentDto.CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        CommentDto.CommentResponse response = commentService.createComment(request, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto.CommentResponse> getComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        CommentDto.CommentResponse response = commentService.getComment(commentId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/thread/{threadId}")
    public ResponseEntity<Page<CommentDto.CommentResponse>> getCommentsForThread(
            @PathVariable Long threadId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<CommentDto.CommentResponse> response = commentService.getCommentsForThread(threadId, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<CommentDto.CommentResponse>> getReplies(
            @PathVariable Long commentId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<CommentDto.CommentResponse> response = commentService.getReplies(commentId, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto.CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDto.CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        CommentDto.CommentResponse response = commentService.updateComment(commentId, request, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Comment deleted successfully"));
    }
    
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentDto.CommentLikeResponse> likeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        CommentDto.CommentLikeResponse response = commentService.likeComment(commentId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<CommentDto.CommentLikeResponse> unlikeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        CommentDto.CommentLikeResponse response = commentService.unlikeComment(commentId, currentUser);
        return ResponseEntity.ok(response);
    }
}