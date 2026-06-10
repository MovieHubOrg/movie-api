package com.movie.api.service;

import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.model.UserMovie;
import com.movie.api.storage.repository.UserMovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserMovieService {
    private static final double MAX_WATCH_PROGRESS_VALUE = 3.0;

    @Autowired
    private UserMovieRepository userMovieRepository;

    @Autowired
    private RedisService redisService;

    @Transactional
    public void saveSignal(Long userId, Long movieId, Long movieItemId, Integer type, String source, Double value) {
        if (userId == null || movieId == null || type == null || source == null) {
            return;
        }

        UserMovie userMovie = userMovieRepository.findFirstByUserIdAndMovieIdAndTypeAndSourceOrderByModifiedDateDesc(
                userId,
                movieId,
                type,
                source
        ).orElse(null);
        saveSignal(userMovie, userId, movieId, movieItemId, type, source, value);
    }

    @Transactional
    public void saveSignalBySource(Long userId, Long movieId, Long movieItemId, Integer type, String source, Double value) {
        if (userId == null || movieId == null || type == null || source == null) {
            return;
        }

        UserMovie userMovie = userMovieRepository.findFirstByUserIdAndMovieIdAndSourceOrderByModifiedDateDesc(
                userId,
                movieId,
                source
        ).orElse(null);
        saveSignal(userMovie, userId, movieId, movieItemId, type, source, value);
    }

    @Transactional
    public void deleteSignal(Long userId, Long movieId, Integer type, String source) {
        if (userId == null || movieId == null || type == null || source == null) {
            return;
        }
        int deletedCount = userMovieRepository.deleteByUserIdAndMovieIdAndTypeAndSource(userId, movieId, type, source);
        if (deletedCount > 0) {
            invalidateRecommendationCache(userId);
        }
    }

    @Transactional
    public void deleteSignalBySource(Long userId, Long movieId, String source) {
        if (userId == null || movieId == null || source == null) {
            return;
        }
        int deletedCount = userMovieRepository.deleteByUserIdAndMovieIdAndSource(userId, movieId, source);
        if (deletedCount > 0) {
            invalidateRecommendationCache(userId);
        }
    }

    public Double calculateWatchProgressValue(Long lastWatchSeconds, Long watchEndSeconds) {
        if (lastWatchSeconds == null || watchEndSeconds == null || watchEndSeconds <= 0) {
            return 0.0;
        }
        double progress = lastWatchSeconds.doubleValue() / watchEndSeconds.doubleValue();
        return Math.max(0.0, Math.min(progress * MAX_WATCH_PROGRESS_VALUE, MAX_WATCH_PROGRESS_VALUE));
    }

    private void saveSignal(UserMovie userMovie, Long userId, Long movieId, Long movieItemId, Integer type, String source, Double value) {
        if (userMovie == null) {
            userMovie = new UserMovie();
            userMovie.setUserId(userId);
            userMovie.setMovieId(movieId);
        }
        userMovie.setMovieItemId(movieItemId);
        userMovie.setType(type);
        userMovie.setSource(source);
        userMovie.setValue(value);
        userMovieRepository.save(userMovie);
        invalidateRecommendationCache(userId);
    }

    private void invalidateRecommendationCache(Long userId) {
        if (userId == null) {
            return;
        }

        try {
            String userRecommendationKey = redisService.buildKey("movie", "recommendation", userId.toString());
            String recentWatchedCategoryKey = redisService.buildKey(
                    "movie",
                    "recommendation",
                    "recent-watched-category",
                    "v3",
                    userId.toString()
            );
            redisService.delete(userRecommendationKey);
            redisService.delete(recentWatchedCategoryKey);
        } catch (Exception ex) {
            log.warn("Failed to invalidate recommendation cache for userId={}", userId, ex);
        }
    }
}
