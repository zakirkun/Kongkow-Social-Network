package app.kongkow.social.thread.service;

import app.kongkow.social.thread.dto.TrendingDto;
import app.kongkow.social.thread.entity.Hashtag;
import app.kongkow.social.thread.entity.Thread;
import app.kongkow.social.thread.repository.HashtagRepository;
import app.kongkow.social.thread.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final ThreadRepository threadRepository;
    
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
    
    @Transactional
    public Set<Hashtag> processHashtags(String content) {
        // Extract hashtags from content
        Matcher matcher = HASHTAG_PATTERN.matcher(content);
        Set<String> hashtagNames = new HashSet<>();
        
        while (matcher.find()) {
            String tag = matcher.group(1).toLowerCase();
            if (tag.length() > 0) {
                hashtagNames.add(tag);
            }
        }
        
        // Process each hashtag
        Set<Hashtag> hashtags = new HashSet<>();
        for (String name : hashtagNames) {
            Hashtag hashtag = hashtagRepository.findByName(name)
                    .orElse(new Hashtag());
            
            if (hashtag.getId() == null) {
                hashtag.setName(name);
                hashtag.setUsageCount(1);
            } else {
                hashtag.setUsageCount(hashtag.getUsageCount() + 1);
            }
            
            hashtags.add(hashtagRepository.save(hashtag));
        }
        
        return hashtags;
    }
    
    @Transactional(readOnly = true)
    public TrendingDto.TrendingHashtagsResponse getTrendingHashtags(String timeframe, Pageable pageable) {
        Page<Hashtag> hashtagPage;
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
        
        hashtagPage = hashtagRepository.findTrendingHashtags(startTime, pageable);
        
        List<TrendingDto.HashtagResponse> hashtags = hashtagPage.getContent().stream()
                .map(this::mapToHashtagResponse)
                .collect(Collectors.toList());
        
        return TrendingDto.TrendingHashtagsResponse.builder()
                .hashtags(hashtags)
                .timeframe(timeframe)
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<TrendingDto.HashtagResponse> searchHashtags(String query, Pageable pageable) {
        // Remove the # if it's present at the beginning
        if (query.startsWith("#")) {
            query = query.substring(1);
        }
        
        return hashtagRepository.findByNameContainingOrderByUsageCountDesc(query, pageable)
                .stream()
                .map(this::mapToHashtagResponse)
                .collect(Collectors.toList());
    }
    
    private TrendingDto.HashtagResponse mapToHashtagResponse(Hashtag hashtag) {
        return TrendingDto.HashtagResponse.builder()
                .id(hashtag.getId())
                .name(hashtag.getName())
                .usageCount(hashtag.getUsageCount())
                .createdAt(hashtag.getCreatedAt())
                .build();
    }
}