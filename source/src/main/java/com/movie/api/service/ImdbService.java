package com.movie.api.service;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.movie.ImdbRatingsSyncDto;
import com.movie.api.dto.movie.OmdbRatingResponseDto;
import com.movie.api.service.feign.FeignOmdbService;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.repository.MovieRepository;
import com.movie.api.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ImdbService {
    private static final String REDIS_KEY_LIMIT_FLAG = "omdb:limit-reached";
    private static final int LIMIT_FLAG_TTL_SECONDS = 3600;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private FeignOmdbService feignOmdbService;

    @Value("${omdb.api.key}")
    private String omdbApiKey;

    public Double fetchRating(String imdbId) {
        if (StringUtils.isNullOrEmpty(imdbId) || StringUtils.isNullOrEmpty(omdbApiKey)) {
            return null;
        }

        if (isDailyLimitReached()) {
            log.warn("OMDb daily limit flag is active, skipping request. imdbId={}", imdbId);
            return null;
        }

        try {
            OmdbRatingResponseDto response = feignOmdbService.getByImdbId(BaseConstant.HEADER_BYPASS, omdbApiKey, imdbId.trim());

            if (isLimitResponse(response)) {
                try {
                    redisService.put(REDIS_KEY_LIMIT_FLAG, true, LIMIT_FLAG_TTL_SECONDS);
                    log.warn("OMDb daily limit flag set for {} hours", LIMIT_FLAG_TTL_SECONDS);
                } catch (Exception ex) {
                    log.warn("Failed to set OMDb limit flag in Redis", ex);
                }
                log.warn("OMDb reported daily limit reached. imdbId={}", imdbId);
                return null;
            }

            return parseRating(response);

        } catch (Exception ex) {
            log.warn("Failed to fetch IMDb rating from OMDb. imdbId={}", imdbId, ex);
            return null;
        }
    }

    @Transactional
    public ImdbRatingsSyncDto syncAllMovieRatings() {
        ImdbRatingsSyncDto result = new ImdbRatingsSyncDto();
        List<Movie> movies = movieRepository.findAllByImdbIdIsNotNull();

        for (Movie movie : movies) {
            Double rating = fetchRating(movie.getImdbId());

            if (rating == null) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                // fetchRating đã log lý do skip (limit / lỗi / null imdbId)
                if (isDailyLimitReached()) {
                    result.setDailyLimitReached(true);
                }
                continue;
            }

            result.setMatchedMovies(result.getMatchedMovies() + 1);

            if (Objects.equals(movie.getImdbRating(), rating)) {
                continue;
            }

            movie.setImdbRating(rating);
            movieRepository.save(movie);
            redisService.delete(redisService.buildKey("movie", movie.getId().toString()));
            result.setUpdatedMovies(result.getUpdatedMovies() + 1);
        }

        if (result.getUpdatedMovies() > 0) {
            redisService.deleteByPrefix(redisService.buildKey("movie", "suggestion"));
            redisService.deleteByPrefix(redisService.buildKey("movie", "recommendation"));
        }
        return result;
    }

    Double parseRating(OmdbRatingResponseDto response) {
        if (response == null
                || !"True".equalsIgnoreCase(response.getResponse())
                || StringUtils.isNullOrEmpty(response.getImdbRating())
                || "N/A".equalsIgnoreCase(response.getImdbRating())) {
            return null;
        }

        try {
            return Double.parseDouble(response.getImdbRating());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    boolean isLimitResponse(OmdbRatingResponseDto response) {
        return response != null
                && "False".equalsIgnoreCase(response.getResponse())
                && response.getError() != null
                && response.getError().toLowerCase().contains("limit");
    }

    boolean isDailyLimitReached() {
        try {
            return Boolean.TRUE.equals(redisService.get(REDIS_KEY_LIMIT_FLAG, Boolean.class));
        } catch (Exception ex) {
            log.warn("Failed to read OMDb limit flag from Redis", ex);
            return false;
        }
    }
}
