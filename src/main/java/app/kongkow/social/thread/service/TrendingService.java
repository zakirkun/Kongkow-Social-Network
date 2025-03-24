package app.kongkow.social.thread.service;

import app.kongkow.social.thread.dto.ThreadDto;
import app.kongkow.social.thread.dto.TrendingDto;
import app.kongkow.social.thread.entity.Hashtag;
import app.kongkow.social.thread.entity.Thread;
import app.kongkow.social.thread.repository.HashtagRepository;
import app.kongkow.social.thread.repository.ThreadRepository;
import app.kongkow.social.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrendingService {

    private final HashtagRepository hashtagRepository;
    private final ThreadRepository threadRepository;
    private final ThreadService threadService;
    private final HashtagService hashtagService;
    
    @Transactional(readOnly = true)
    public TrendingDto.TrendingHashtagsResponse getTrendingHashtags(String timeframe, Pageable pageable) {
        return hashtagService.getTrendingHashtags(timeframe, pageable);
    }
    
    @Transactional(readOnly = true)
    public TrendingDto.TrendingThreadsResponse getTrendingThreads(String timeframe, Pageable pageable, User currentUser) {
        LocalDateTime startTime;
        
        switch (timeframe.toLowerCase()) {
            case "24h":
                startTime = LocalDateTime.now().minusDays(1);
                break;
            case "week":
                startTime = LocalDateTime.now().minusWeeks(1);
                break;
            case "month":
                startTime = LocalDateTime.now().minusMonths(1);
                break;
            default:
                startTime = LocalDateTime.now().minusDays(1);
                timeframe = "24h";
        }
        
        // Here we would ideally use a custom query to get trending threads
        // For now, we'll just use a simple approach based on creation time
        Page<Thread> threadsPage = threadRepository.findByCreatedAtGreaterThanAndParentIsNullAndDeletedFalseOrderByViewCountDesc(
                startTime, pageable);
        
        List<ThreadDto.ThreadResponse> threads = threadsPage.getContent().stream()
                .map(thread -> threadService.mapToThreadResponse(thread, currentUser))
                .collect(Collectors.toList());
        
        return TrendingDto.TrendingThreadsResponse.builder()
                .threads(threads)
                .timeframe(timeframe)
                .build();
    }
    
    @Transactional(readOnly = true)
    public TrendingDto.TrendingThreadsResponse getThreadsByHashtag(String hashtagName, Pageable pageable, User currentUser) {
        Optional<Hashtag> hashtagOpt = hashtagRepository.findByName(hashtagName.toLowerCase());
        
        if (hashtagOpt.isPresent()) {
            Hashtag hashtag = hashtagOpt.get();
            Page<Thread> threadsPage = threadRepository.findByHashtagsContainsAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(
                    hashtag, pageable);
            
            List<ThreadDto.ThreadResponse> threads = threadsPage.getContent().stream()
                    .map(thread -> threadService.mapToThreadResponse(thread, currentUser))
                    .collect(Collectors.toList());
            
            return TrendingDto.TrendingThreadsResponse.builder()
                    .threads(threads)
                    .timeframe("all")
                    .build();
        } else {
            return TrendingDto.TrendingThreadsResponse.builder()
                    .threads(List.of())
                    .timeframe("all")
                    .build();
        }
    }
    
    @Transactional(readOnly = true)
    public TrendingDto.SearchResponse searchThreadsAndHashtags(String query, Pageable pageable, User currentUser) {
        // Search for threads that contain the query
        Page<Thread> threadsPage = threadRepository.findByContentContainingAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(
                query, pageable);
        
        List<ThreadDto.ThreadResponse> threads = threadsPage.getContent().stream()
                .map(thread -> threadService.mapToThreadResponse(thread, currentUser))
                .collect(Collectors.toList());
        
        // Search for hashtags
        List<TrendingDto.HashtagResponse> hashtags = hashtagService.searchHashtags(query, pageable);
        
        return TrendingDto.SearchResponse.builder()
                .threads(threads)
                .hashtags(hashtags)
                .query(query)
                .totalResults(threads.size() + hashtags.size())
                .build();
    }
}