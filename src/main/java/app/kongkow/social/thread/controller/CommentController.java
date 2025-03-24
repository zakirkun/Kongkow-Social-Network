package app.kongkow.social.thread.controller;
import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.common.payload.ApiResponse;
import app.kongkow.social.thread.dto.CommentDto;
import app.kongkow.social.thread.service.CommentService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Comments", description = "Comment management and interaction APIs")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    @Operation(summary = "Create a comment", description = "Create a new comment on a thread or reply to another comment")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment created successfully", 
                     content = @Content(schema = @Schema(implementation = CommentDto.CommentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Thread or parent comment not found")
    })
    @PostMapping
    public ResponseEntity<CommentDto.CommentResponse> createComment(
            @Valid @RequestBody CommentDto.CommentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        CommentDto.CommentResponse response = commentService.createComment(request, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get a comment", description = "Get a specific comment by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = CommentDto.CommentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto.CommentResponse> getComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        CommentDto.CommentResponse response = commentService.getComment(commentId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get comments for a thread", description = "Get all top-level comments for a specific thread")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Thread not found")
    })
    @GetMapping("/thread/{threadId}")
    public ResponseEntity<Page<CommentDto.CommentResponse>> getCommentsForThread(
            @PathVariable Long threadId,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<CommentDto.CommentResponse> response = commentService.getCommentsForThread(threadId, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get replies to a comment", description = "Get all replies to a specific comment")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Replies retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<CommentDto.CommentResponse>> getReplies(
            @PathVariable Long commentId,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        
        Page<CommentDto.CommentResponse> response = commentService.getReplies(commentId, pageable, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update a comment", description = "Update the content of an existing comment")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment updated successfully", 
                     content = @Content(schema = @Schema(implementation = CommentDto.CommentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the comment owner"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto.CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDto.CommentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        CommentDto.CommentResponse response = commentService.updateComment(commentId, request, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Delete a comment", description = "Delete an existing comment")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment deleted successfully", 
                     content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the comment owner"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Comment deleted successfully"));
    }
    
    @Operation(summary = "Like a comment", description = "Like a specific comment")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment liked successfully", 
                     content = @Content(schema = @Schema(implementation = CommentDto.CommentLikeResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentDto.CommentLikeResponse> likeComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        CommentDto.CommentLikeResponse response = commentService.likeComment(commentId, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Unlike a comment", description = "Remove a like from a specific comment")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment unliked successfully", 
                     content = @Content(schema = @Schema(implementation = CommentDto.CommentLikeResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<CommentDto.CommentLikeResponse> unlikeComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        CommentDto.CommentLikeResponse response = commentService.unlikeComment(commentId, currentUser);
        return ResponseEntity.ok(response);
    }
}