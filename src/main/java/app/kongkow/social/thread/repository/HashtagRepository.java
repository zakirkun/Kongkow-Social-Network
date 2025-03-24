package app.kongkow.social.thread.repository;

import app.kongkow.social.thread.entity.Hashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    // Find a hashtag by its name
    Optional<Hashtag> findByName(String name);
    
    // Check if a hashtag exists
    boolean existsByName(String name);
    
    // Find trending hashtags based on usage count
    Page<Hashtag> findByOrderByUsageCountDesc(Pageable pageable);
    
    // Find trending hashtags within a specific time period
    @Query("SELECT h FROM Hashtag h JOIN h.threads t WHERE t.createdAt >= :startTime GROUP BY h ORDER BY COUNT(t) DESC")
    Page<Hashtag> findTrendingHashtags(@Param("startTime") LocalDateTime startTime, Pageable pageable);
    
    // Find hashtags that contain a specific string (for autocomplete)
    List<Hashtag> findByNameContainingOrderByUsageCountDesc(String partial, Pageable pageable);
}