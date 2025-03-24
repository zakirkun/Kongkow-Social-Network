package app.kongkow.social.thread.repository;

import app.kongkow.social.thread.entity.Comment;
import app.kongkow.social.thread.entity.CommentLike;
import app.kongkow.social.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // Find a like by user and comment
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);
    
    // Check if a user has liked a comment
    boolean existsByUserAndComment(User user, Comment comment);
    
    // Count likes for a comment
    long countByComment(Comment comment);
    
    // Delete like by user and comment
    void deleteByUserAndComment(User user, Comment comment);
}