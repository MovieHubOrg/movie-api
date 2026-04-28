package com.movie.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.dto.review.ReviewStatisticsDto;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.movie.FilterMovieForm;
import com.movie.api.form.movie.MovieMetadataForm;
import com.movie.api.mapper.MovieItemMapper;
import com.movie.api.mapper.MovieMapper;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.criteria.MovieCriteria;
import com.movie.api.storage.model.Category;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.MovieItem;
import com.movie.api.storage.repository.MovieRepository;
import com.movie.api.storage.repository.UserMovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieItemMapper movieItemMapper;

    @Autowired
    private UserMovieRepository userMovieRepository;

    /**
     * Calculate reviewCount và averageRating for Movie.
     *
     * @param movieId ID Movie
     * @param rating  rating Review
     * @param action  1 = add, -1 = remove
     */
    public ReviewStatisticsDto calculateReview(Long movieId, int rating, int action) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Movie not found"));

        long oldCount = Optional.ofNullable(movie.getReviewCount()).orElse(0L);
        double oldAverage = Optional.ofNullable(movie.getAverageRating()).orElse(0.0);
        rating = rating * 2;
        long newCount = 0L;
        double newAverage = 0.0;

        if (action == BaseConstant.ACTION_ADD) { // ADD review
            newCount = oldCount + 1;
            newAverage = ((oldAverage * oldCount) + rating) / newCount;
            movie.setReviewCount(newCount);
            movie.setAverageRating(newAverage);

        } else if (action == BaseConstant.ACTION_DELETE && oldCount > 1) { // DELETE review
            newCount = oldCount - 1;
            newAverage = ((oldAverage * oldCount) - rating) / newCount;
            movie.setReviewCount(newCount);
            movie.setAverageRating(newAverage);
        }
        movie.setReviewCount(newCount);
        movie.setAverageRating(newAverage);
        movieRepository.save(movie);

        String key = redisService.buildKey("movie", movieId.toString());
        log.debug("========> key {}", key);
        MovieDto movieDto = redisService.get(key, MovieDto.class);
        if (movieDto != null) {
            movieDto.setAverageRating(newAverage);
            movieDto.setReviewCount(newCount);
            redisService.put(key, movieDto, 5 * 60);
            log.debug("Updated movie review cache for movieId {}", movieId);
        }

        ReviewStatisticsDto statistics = new ReviewStatisticsDto();
        statistics.setReviewCount(newCount);
        statistics.setAverageRating(newAverage);
        return statistics;
    }

    /**
     * Calculate commentCount for Movie.
     *
     * @param movieId id of Movie
     * @param action  1 = add, -1 = remove
     */
    public void calculateComment(Long movieId, int action) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Movie not found"));

        long commentCount = movie.getCommentCount() != null ? movie.getCommentCount() : 0L;
        if (action == BaseConstant.ACTION_ADD) {
            commentCount += 1;
        } else if (action == BaseConstant.ACTION_DELETE) {
            if (commentCount > 0) {
                commentCount -= 1;
            }
        }
        movie.setCommentCount(commentCount);
        movieRepository.save(movie);

        String key = redisService.buildKey("movie", movie.getId().toString());
        log.debug("========> key {}", key);
        MovieDto movieDto = redisService.get(key, MovieDto.class);
        if (movieDto != null) {
            movieDto.setCommentCount(commentCount);
            redisService.put(key, movieDto, 5 * 60);
            log.debug("Updated movie commentCount cache for movieId {}", movie.getId());
        }
    }

    public FilterMovieForm parseFilterMovie(String filterString) {
        FilterMovieForm filter;
        try {
            filter = objectMapper.readValue(filterString, FilterMovieForm.class);
        } catch (Exception e) {
            log.error("Failed to parse filter JSON for collection: {}", filterString, e);
            filter = new FilterMovieForm();
        }
        return filter;
    }

    public List<Movie> getMovieForCollection(String filterString, List<Long> existedMovieIds) {
        FilterMovieForm filter = parseFilterMovie(filterString);
        MovieCriteria criteria = movieMapper.fromFilterMovieFromToMovieCriteria(filter);
        criteria.setExcludeIds(existedMovieIds);
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);

        int limit = filter.getLimit() == null ? 10 : filter.getLimit();
        Pageable pageable = PageRequest.of(0, limit);

        return movieRepository.findAll(criteria.getSpecification(), pageable).getContent();
    }

    public List<Movie> findSuggestedMovies(Movie movie, int limit) {
        if (movie == null || movie.getId() == null || movie.getCategories() == null || movie.getCategories().isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> categoryIds = movie.getCategories().stream()
                .map(Category::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        int pageSize = limit > 0 ? limit : 10;
        return movieRepository.findSuggestion(
                movie.getId(),
                categoryIds,
                movie.getCountry(),
                movie.getLanguage(),
                movie.getType(),
                PageRequest.of(0, pageSize)
        );
    }

    public List<Long> findInterestedUserIds(Movie movie) {
        List<Long> suggestedMovieIds = findSuggestedMovies(movie, 5).stream()
                .map(Movie::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (suggestedMovieIds.isEmpty()) {
            return Collections.emptyList();
        }

        return userMovieRepository.findDistinctUserIdsByMovieIds(suggestedMovieIds, BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER);
    }

    public void updateMetaDataMovie(MovieItem movieItem) {
        Movie movie = movieItem.getMovie();
        try {
            MovieMetadataForm metadata = (movie.getMetadata() != null && !movie.getMetadata().isEmpty())
                    ? objectMapper.readValue(movie.getMetadata(), MovieMetadataForm.class)
                    : new MovieMetadataForm();

            if (Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SINGLE)) {
                if (Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON)) {
                    metadata.setLatestSeason(movieItemMapper.entityToMovieItemMetadataDto(movieItem));
                }
                if (movieItem.getVideo() != null) {
                    metadata.setDuration(movieItem.getVideo().getDuration());
                }
            } else {
                if (Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON)) {
                    metadata.setLatestSeason(movieItemMapper.entityToMovieItemMetadataDto(movieItem));
                } else {
                    metadata.setLatestSeason(movieItemMapper.entityToMovieItemMetadataDto(movieItem.getParent()));
                    metadata.setLatestEpisode(movieItemMapper.entityToMovieItemMetadataDto(movieItem));
                }
            }
            movie.setMetadata(objectMapper.writeValueAsString(metadata));
            movieRepository.save(movie);
        } catch (Exception ex) {
            log.error("Failed to parse metadata JSON for movie: {}", movie.getId(), ex);
        }
    }

    public void resetMetaDataMovie(Movie movie) {
        if (movie.getMetadata() != null && !movie.getMetadata().isEmpty()) {
            try {
                if (Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SINGLE)) {
                    movie.setMetadata(null);
                } else {
                    MovieMetadataForm metadata = objectMapper.readValue(movie.getMetadata(), MovieMetadataForm.class);
                    metadata.setLatestSeason(null);
                    metadata.setLatestEpisode(null);
                    movie.setMetadata(objectMapper.writeValueAsString(metadata));
                }
                movieRepository.save(movie);
            } catch (Exception ex) {
                log.error("Failed to parse metadata JSON for movie id: {}", movie.getId(), ex);
            }
        }
    }

    public void clearLatestMetadata(Movie movie, boolean clearLatestSeason, boolean clearLatestEpisode) {
        try {
            MovieMetadataForm metadata = (movie.getMetadata() != null && !movie.getMetadata().isEmpty())
                    ? objectMapper.readValue(movie.getMetadata(), MovieMetadataForm.class)
                    : new MovieMetadataForm();

            if (clearLatestSeason) {
                metadata.setLatestSeason(null);
            }
            if (clearLatestEpisode) {
                metadata.setLatestEpisode(null);
            }

            movie.setMetadata(objectMapper.writeValueAsString(metadata));
            movieRepository.save(movie);
        } catch (Exception ex) {
            log.error("Failed to clear metadata JSON for movie: {}", movie.getId(), ex);
        }
    }

    public Date resolveScheduleAt(Date requestedScheduleAt, Date releaseDate) {
        Date base = (requestedScheduleAt != null) ? requestedScheduleAt : new Date();
        return base.before(releaseDate) ? base : releaseDate;
    }
}
