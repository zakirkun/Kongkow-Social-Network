package app.kongkow.social.thread.repository;

import app.kongkow.social.thread.entity.Like;
import app.kongkow.social.thread.entity.Thread;
import app.kongkow.social.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // Find a like by user and thread
    Optional<Like> findByUserAndThread(User user, Thread thread);
    
    // Check if a user has liked a thread
    boolean existsByUserAndThread(User user, Thread thread);
    
    // Count likes for a thread
    long countByThread(Thread thread);
    
    // Delete like by user and thread
    void deleteByUserAndThread(User user, Thread thread);
}