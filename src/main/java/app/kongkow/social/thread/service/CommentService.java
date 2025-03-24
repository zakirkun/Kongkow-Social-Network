package app.kongkow.social.thread.service;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.social.thread.dto.CommentDto;
import app.kongkow.social.thread.entity.Comment;
import app.kongkow.social.thread.entity.CommentLike;
import app.kongkow.social.thread.entity.Thread;
import app.kongkow.social.thread.repository.CommentLikeRepository;
import app.kongkow.social.thread.repository.CommentRepository;
import app.kongkow.social.thread.repository.ThreadRepository;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public CommentDto.CommentResponse createComment(CommentDto.CommentRequest request, User currentUser) {
        Thread thread = threadRepository.findById(request.getThreadId())
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + request.getThreadId()));
        
        if (thread.isDeleted()) {
            throw new ResourceNotFoundException("Thread not found with id: " + request.getThreadId());
        }
        
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(currentUser);
        comment.setThread(thread);
        
        // If it's a reply to another comment, set the parent
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found with id: " + request.getParentId()));
            
            if (parent.isDeleted()) {
                throw new ResourceNotFoundException("Parent comment not found with id: " + request.getParentId());
            }
            
            comment.setParent(parent);
        }
        
        Comment savedComment = commentRepository.save(comment);
        
        return mapToCommentResponse(savedComment, currentUser);
    }
    
    @Transactional(readOnly = true)
    public CommentDto.CommentResponse getComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        if (comment.isDeleted()) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        
        return mapToCommentResponse(comment, currentUser);
    }
    
    @Transactional(readOnly = true)
    public Page<CommentDto.CommentResponse> getCommentsForThread(Long threadId, Pageable pageable, User currentUser) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));
        
        if (thread.isDeleted()) {
            throw new ResourceNotFoundException("Thread not found with id: " + threadId);
        }
        
        Page<Comment> comments = commentRepository.findByThreadAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(thread, pageable);
        
        return comments.map(comment -> mapToCommentResponse(comment, currentUser));
    }
    
    @Transactional(readOnly = true)
    public Page<CommentDto.CommentResponse> getReplies(Long commentId, Pageable pageable, User currentUser) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        if (parentComment.isDeleted()) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        
        Page<Comment> replies = commentRepository.findByParentAndDeletedFalseOrderByCreatedAtDesc(parentComment, pageable);
        
        return replies.map(reply -> mapToCommentResponse(reply, currentUser));
    }
    
    @Transactional
    public CommentDto.CommentResponse updateComment(Long commentId, CommentDto.CommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to update this comment");
        }
        
        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        
        return mapToCommentResponse(updatedComment, currentUser);
    }
    
    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this comment");
        }
        
        // Soft delete - mark as deleted but keep in database
        comment.setDeleted(true);
        commentRepository.save(comment);
    }
    
    @Transactional
    public CommentDto.CommentLikeResponse likeComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        if (comment.isDeleted()) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        
        // Check if user already liked the comment
        boolean alreadyLiked = commentLikeRepository.existsByUserAndComment(currentUser, comment);
        
        if (!alreadyLiked) {
            CommentLike like = CommentLike.builder()
                    .user(currentUser)
                    .comment(comment)
                    .build();
            commentLikeRepository.save(like);
        }
        
        long likeCount = commentLikeRepository.countByComment(comment);
        
        return CommentDto.CommentLikeResponse.builder()
                .liked(true)
                .likeCount((int) likeCount)
                .build();
    }
    
    @Transactional
    public CommentDto.CommentLikeResponse unlikeComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        if (comment.isDeleted()) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        
        commentLikeRepository.deleteByUserAndComment(currentUser, comment);
        
        long likeCount = commentLikeRepository.countByComment(comment);
        
        return CommentDto.CommentLikeResponse.builder()
                .liked(false)
                .likeCount((int) likeCount)
                .build();
    }
    
    private CommentDto.CommentResponse mapToCommentResponse(Comment comment, User currentUser) {
        // Get like count
        long likeCount = commentLikeRepository.countByComment(comment);
        
        // Get reply count
        long replyCount = commentRepository.countByParentAndDeletedFalse(comment);
        
        // Check if current user has liked the comment
        boolean liked = false;
        if (currentUser != null) {
            liked = commentLikeRepository.existsByUserAndComment(currentUser, comment);
        }
        
        // Get recent replies (if any)
        List<CommentDto.CommentResponse> recentReplies = new ArrayList<>();
        if (replyCount > 0) {
            Page<Comment> replies = commentRepository.findByParentAndDeletedFalseOrderByCreatedAtDesc(comment, Pageable.ofSize(3));
            recentReplies = replies.getContent().stream()
                    .map(reply -> mapToSimpleCommentResponse(reply, currentUser))
                    .collect(Collectors.toList());
        }
        
        return CommentDto.CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(CommentDto.UserSummary.builder()
                        .id(comment.getUser().getId())
                        .username(comment.getUser().getUsername())
                        .fullName(comment.getUser().getFullName())
                        .profilePicture(comment.getUser().getProfilePicture())
                        .build())
                .threadId(comment.getThread().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .likeCount((int) likeCount)
                .replyCount((int) replyCount)
                .liked(liked)
                .recentReplies(recentReplies)
                .build();
    }
    
    // Simplified version for nested replies
    private CommentDto.CommentResponse mapToSimpleCommentResponse(Comment comment, User currentUser) {
        // Get like count
        long likeCount = commentLikeRepository.countByComment(comment);
        
        // Get reply count
        long replyCount = commentRepository.countByParentAndDeletedFalse(comment);
        
        // Check if current user has liked the comment
        boolean liked = false;
        if (currentUser != null) {
            liked = commentLikeRepository.existsByUserAndComment(currentUser, comment);
        }
        
        return CommentDto.CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(CommentDto.UserSummary.builder()
                        .id(comment.getUser().getId())
                        .username(comment.getUser().getUsername())
                        .fullName(comment.getUser().getFullName())
                        .profilePicture(comment.getUser().getProfilePicture())
                        .build())
                .threadId(comment.getThread().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .likeCount((int) likeCount)
                .replyCount((int) replyCount)
                .liked(liked)
                .recentReplies(null)  // Don't include nested replies to avoid recursion
                .build();
    }
}