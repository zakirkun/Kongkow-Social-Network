package app.kongkow.social.thread.repository;

import app.kongkow.social.thread.entity.Comment;
import app.kongkow.social.thread.entity.Thread;
import app.kongkow.social.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find all top-level comments (not replies) for a thread
    Page<Comment> findByThreadAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(Thread thread, Pageable pageable);
    
    // Find all comments by a specific user
    Page<Comment> findByUserAndDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find replies to a specific comment
    Page<Comment> findByParentAndDeletedFalseOrderByCreatedAtDesc(Comment parent, Pageable pageable);
    
    // Count comments for a thread
    long countByThreadAndDeletedFalse(Thread thread);
    
    // Count replies for a comment
    long countByParentAndDeletedFalse(Comment parent);
    
    // Get recent comments for a thread
    List<Comment> findTop5ByThreadAndDeletedFalseOrderByCreatedAtDesc(Thread thread);
}