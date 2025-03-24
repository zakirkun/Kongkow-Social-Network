package app.kongkow.social.thread.repository;

import app.kongkow.social.thread.entity.Media;
import app.kongkow.social.thread.entity.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    // Find all media related to a specific thread
    List<Media> findByThreadOrderByCreatedAtAsc(Thread thread);
    
    // Find media by ID and thread
    Media findByIdAndThread(Long id, Thread thread);
    
    // Delete all media related to a thread
    void deleteByThread(Thread thread);
}