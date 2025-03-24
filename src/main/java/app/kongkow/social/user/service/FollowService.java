package app.kongkow.social.user.service;

import app.kongkow.common.exception.ResourceNotFoundException;
import app.kongkow.social.user.dto.FollowDto;
import app.kongkow.social.user.entity.Follow;
import app.kongkow.social.user.entity.User;
import app.kongkow.social.user.repository.FollowRepository;
import app.kongkow.social.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public FollowDto.FollowResponse followUser(Long targetUserId, User currentUser) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }
        
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));
        
        // Check if already following
        boolean alreadyFollowing = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
        
        if (!alreadyFollowing) {
            Follow follow = Follow.builder()
                    .follower(currentUser)
                    .following(targetUser)
                    .build();
            followRepository.save(follow);
        }
        
        // Get updated counts
        long followerCount = followRepository.countByFollowing(targetUser);
        long followingCount = followRepository.countByFollower(targetUser);
        
        return FollowDto.FollowResponse.builder()
                .following(true)
                .followerCount((int) followerCount)
                .followingCount((int) followingCount)
                .build();
    }
    
    @Transactional
    public FollowDto.FollowResponse unfollowUser(Long targetUserId, User currentUser) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));
        
        followRepository.deleteByFollowerAndFollowing(currentUser, targetUser);
        
        // Get updated counts
        long followerCount = followRepository.countByFollowing(targetUser);
        long followingCount = followRepository.countByFollower(targetUser);
        
        return FollowDto.FollowResponse.builder()
                .following(false)
                .followerCount((int) followerCount)
                .followingCount((int) followingCount)
                .build();
    }
    
    @Transactional(readOnly = true)
    public boolean isFollowing(User follower, User following) {
        return followRepository.existsByFollowerAndFollowing(follower, following);
    }
    
    @Transactional(readOnly = true)
    public FollowDto.FollowResponse getFollowStatus(Long targetUserId, User currentUser) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));
        
        boolean following = followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
        long followerCount = followRepository.countByFollowing(targetUser);
        long followingCount = followRepository.countByFollower(targetUser);
        
        return FollowDto.FollowResponse.builder()
                .following(following)
                .followerCount((int) followerCount)
                .followingCount((int) followingCount)
                .build();
    }
    
    @Transactional(readOnly = true)
    public Page<FollowDto.FollowerResponse> getFollowers(Long userId, User currentUser, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Page<Follow> followers = followRepository.findByFollowing(user, pageable);
        
        return followers.map(follow -> {
            User follower = follow.getFollower();
            boolean isFollowing = false;
            
            if (currentUser != null) {
                isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, follower);
            }
            
            return FollowDto.FollowerResponse.builder()
                    .id(follower.getId())
                    .username(follower.getUsername())
                    .fullName(follower.getFullName())
                    .profilePicture(follower.getProfilePicture())
                    .followedAt(follow.getCreatedAt())
                    .following(isFollowing)
                    .build();
        });
    }
    
    @Transactional(readOnly = true)
    public Page<FollowDto.FollowingResponse> getFollowing(Long userId, User currentUser, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Page<Follow> following = followRepository.findByFollower(user, pageable);
        
        return following.map(follow -> {
            User followed = follow.getFollowing();
            
            return FollowDto.FollowingResponse.builder()
                    .id(followed.getId())
                    .username(followed.getUsername())
                    .fullName(followed.getFullName())
                    .profilePicture(followed.getProfilePicture())
                    .followedAt(follow.getCreatedAt())
                    .following(true) // Always true since this is the list of users being followed
                    .build();
        });
    }
}