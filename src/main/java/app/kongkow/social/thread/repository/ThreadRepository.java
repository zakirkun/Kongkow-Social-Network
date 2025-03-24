package app.kongkow.social.thread.repository;

import app.kongkow.social.thread.entity.Hashtag;
import app.kongkow.social.thread.entity.Thread;
import app.kongkow.social.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {

    // Find all root threads (not replies) by a specific user
    Page<Thread> findByUserAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find all threads (including replies) by a specific user
    Page<Thread> findByUserAndDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find replies to a specific thread
    Page<Thread> findByParentAndDeletedFalseOrderByCreatedAtDesc(Thread parent, Pageable pageable);
    
    // Find all root threads from users followed by the current user
    @Query("SELECT t FROM Thread t WHERE t.user.id IN " +
           "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) " +
           "AND t.parent IS NULL AND t.deleted = false ORDER BY t.createdAt DESC")
    Page<Thread> findThreadsFromFollowedUsers(@Param("userId") Long userId, Pageable pageable);
    
    // Find recent threads for a public timeline (excluding replies)
    Page<Thread> findByParentIsNullAndDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    // Count replies for a thread
    long countByParentAndDeletedFalse(Thread parent);
    
    // Find threads by hashtag
    Page<Thread> findByHashtagsContainsAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(
            Hashtag hashtag, Pageable pageable);
            
    // Find threads by content search
    Page<Thread> findByContentContainingAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(
            String query, Pageable pageable);
            
    // Find trending threads based on view count within a time period
    Page<Thread> findByCreatedAtGreaterThanAndParentIsNullAndDeletedFalseOrderByViewCountDesc(
            LocalDateTime startTime, Pageable pageable);
}