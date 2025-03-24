package app.kongkow.social.user.repository;

import app.kongkow.social.user.entity.Follow;
import app.kongkow.social.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Find a follow relationship between two users
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    // Check if a follow relationship exists
    boolean existsByFollowerAndFollowing(User follower, User following);
    
    // Get all users that a user is following
    Page<Follow> findByFollower(User follower, Pageable pageable);
    
    // Get all followers of a user
    Page<Follow> findByFollowing(User following, Pageable pageable);
    
    // Count how many users a user is following
    long countByFollower(User follower);
    
    // Count how many followers a user has
    long countByFollowing(User following);
    
    // Delete a follow relationship
    void deleteByFollowerAndFollowing(User follower, User following);
}