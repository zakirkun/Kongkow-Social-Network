package app.kongkow.social.thread.service;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.social.thread.dto.ThreadDto;
import app.kongkow.social.thread.entity.Hashtag;
import app.kongkow.social.thread.entity.Like;
import app.kongkow.social.thread.entity.Media;
import app.kongkow.social.thread.entity.Thread;
import app.kongkow.social.thread.repository.LikeRepository;
import app.kongkow.social.thread.repository.ThreadRepository;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final MediaService mediaService;
    private final HashtagService hashtagService;
    
    @Transactional
    public ThreadDto.ThreadResponse createThread(String username, ThreadDto.ThreadRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Thread thread = new Thread();
        thread.setContent(request.getContent());
        thread.setUser(user);
        
        // If it's a reply, set the parent
        if (request.getParentId() != null) {
            Thread parent = threadRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent thread not found with id: " + request.getParentId()));
            thread.setParent(parent);
        }
        
        // Process hashtags
        Set<Hashtag> hashtags = hashtagService.processHashtags(request.getContent());
        thread.setHashtags(hashtags);
        
        Thread savedThread = threadRepository.save(thread);
        
        return mapToThreadResponse(savedThread, user);
    }
    
    @Transactional
    public ThreadDto.ThreadResponse createThreadWithMedia(String username, ThreadDto.ThreadWithMediaRequest request) 
            throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Thread thread = new Thread();
        thread.setContent(request.getContent());
        thread.setUser(user);
        
        // If it's a reply, set the parent
        if (request.getParentId() != null) {
            Thread parent = threadRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent thread not found with id: " + request.getParentId()));
            thread.setParent(parent);
        }
        
        // Process hashtags
        Set<Hashtag> hashtags = hashtagService.processHashtags(request.getContent());
        thread.setHashtags(hashtags);
        
        Thread savedThread = threadRepository.save(thread);
        
        // Process and save media files
        if (request.getMedia() != null && !request.getMedia().isEmpty()) {
            List<Media> media = mediaService.saveMedia(request.getMedia(), savedThread);
            savedThread.setMedia(media);
        }
        
        return mapToThreadResponse(savedThread, user);
    }
    
    @Transactional
    public ThreadDto.ThreadResponse getThread(Long threadId, User currentUser) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));
        
        if (thread.isDeleted()) {
            throw new ResourceNotFoundException("Thread not found with id: " + threadId);
        }
        
        // Increment view count
        thread.incrementViewCount();
        threadRepository.save(thread);
        
        return mapToThreadResponse(thread, currentUser);
    }
    
    @Transactional(readOnly = true)
    public Page<ThreadDto.ThreadResponse> getThreadsByUser(String username, Pageable pageable, User currentUser) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Page<Thread> threads = threadRepository.findByUserAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(user, pageable);
        
        return threads.map(thread -> mapToThreadResponse(thread, currentUser));
    }
    
    @Transactional(readOnly = true)
    public Page<ThreadDto.ThreadResponse> getReplies(Long threadId, Pageable pageable, User currentUser) {
        Thread parentThread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));
        
        if (parentThread.isDeleted()) {
            throw new ResourceNotFoundException("Thread not found with id: " + threadId);
        }
        
        Page<Thread> replies = threadRepository.findByParentAndDeletedFalseOrderByCreatedAtDesc(parentThread, pageable);
        
        return replies.map(reply -> mapToThreadResponse(reply, currentUser));
    }
    
    @Transactional(readOnly = true)
    public Page<ThreadDto.ThreadResponse> getFeed(User currentUser, Pageable pageable) {
        Page<Thread> threads = threadRepository.findThreadsFromFollowedUsers(currentUser.getId(), pageable);
        
        return threads.map(thread -> mapToThreadResponse(thread, currentUser));
    }
    
    @Transactional(readOnly = true)
    public Page<ThreadDto.ThreadResponse> getPublicTimeline(Pageable pageable, User currentUser) {
        Page<Thread> threads = threadRepository.findByParentIsNullAndDeletedFalseOrderByCreatedAtDesc(pageable);
        
        return threads.map(thread -> mapToThreadResponse(thread, currentUser));
    }
    
    @Transactional
    public ThreadDto.ThreadResponse updateThread(Long threadId, ThreadDto.ThreadRequest request, User currentUser) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));
        
        if (!thread.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to update this thread");
        }
        
        thread.setContent(request.getContent());
        
        // Update hashtags
        Set<Hashtag> hashtags = hashtagService.processHashtags(request.getContent());
        thread.setHashtags(hashtags);
        
        Thread updatedThread = threadRepository.save(thread);
        
        return mapToThreadResponse(updatedThread, currentUser);
    }
    
    @Transactional
    public void deleteThread(Long threadId, User currentUser) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));
        
        if (!thread.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this thread");
        }
        
        // Soft delete - mark as deleted but keep in database
        thread.setDeleted(true);
        threadRepository.save(thread);
        
        // Delete any associated media files
        mediaService.deleteAllThreadMedia(thread);
    }
    
    @Transactional
    public ThreadDto.LikeResponse likeThread(Long threadId, User currentUser) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));
        
        if (thread.isDeleted()) {
            throw new ResourceNotFoundException("Thread not found with id: " + threadId);
        }
        
        // Check if user already liked the thread
        boolean alreadyLiked = likeRepository.existsByUserAndThread(currentUser, thread);
        
        if (!alreadyLiked) {
            Like like = Like.builder()
                    .user(currentUser)
                    .thread(thread)
                    .build();
            likeRepository.save(like);
        }
        
        long likeCount = likeRepository.countByThread(thread);
        
        return ThreadDto.LikeResponse.builder()
                .liked(true)
                .likeCount((int) likeCount)
                .build();
    }
    
    @Transactional
    public ThreadDto.LikeResponse unlikeThread(Long threadId, User currentUser) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found with id: " + threadId));
        
        if (thread.isDeleted()) {
            throw new ResourceNotFoundException("Thread not found with id: " + threadId);
        }
        
        likeRepository.deleteByUserAndThread(currentUser, thread);
        
        long likeCount = likeRepository.countByThread(thread);
        
        return ThreadDto.LikeResponse.builder()
                .liked(false)
                .likeCount((int) likeCount)
                .build();
    }
    
    // Make this method public so it can be used by TrendingService
    public ThreadDto.ThreadResponse mapToThreadResponse(Thread thread, User currentUser) {
        // Get like count
        long likeCount = likeRepository.countByThread(thread);
        
        // Get reply count
        long replyCount = threadRepository.countByParentAndDeletedFalse(thread);
        
        // Check if current user has liked the thread
        boolean liked = false;
        if (currentUser != null) {
            liked = likeRepository.existsByUserAndThread(currentUser, thread);
        }
        
        // Map media
        List<ThreadDto.MediaResponse> mediaList = thread.getMedia().stream()
                .map(media -> ThreadDto.MediaResponse.builder()
                        .id(media.getId())
                        .mediaType(media.getMediaType())
                        .mediaUrl(media.getMediaUrl())
                        .mediaAlt(media.getMediaAlt())
                        .build())
                .collect(Collectors.toList());
        
        return ThreadDto.ThreadResponse.builder()
                .id(thread.getId())
                .content(thread.getContent())
                .user(ThreadDto.UserSummary.builder()
                        .id(thread.getUser().getId())
                        .username(thread.getUser().getUsername())
                        .fullName(thread.getUser().getFullName())
                        .profilePicture(thread.getUser().getProfilePicture())
                        .build())
                .parentId(thread.getParent() != null ? thread.getParent().getId() : null)
                .media(mediaList)
                .createdAt(thread.getCreatedAt())
                .updatedAt(thread.getUpdatedAt())
                .likeCount((int) likeCount)
                .replyCount((int) replyCount)
                .liked(liked)
                .build();
    }
}