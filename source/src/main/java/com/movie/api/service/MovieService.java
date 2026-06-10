package com.movie.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.dto.movie.RecentWatchedCategoryRecommendationDto;
import com.movie.api.dto.review.ReviewStatisticsDto;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.movie.FilterMovieForm;
import com.movie.api.form.movie.MovieMetadataForm;
import com.movie.api.mapper.CategoryMapper;
import com.movie.api.mapper.MovieItemMapper;
import com.movie.api.mapper.MovieMapper;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.criteria.MovieCriteria;
import com.movie.api.storage.model.Category;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.MovieSimilarity;
import com.movie.api.storage.model.MovieItem;
import com.movie.api.storage.model.UserMovie;
import com.movie.api.storage.model.UserMovieRecommendation;
import com.movie.api.storage.model.UserMovieScore;
import com.movie.api.storage.repository.MovieSimilarityRepository;
import com.movie.api.storage.repository.MovieRepository;
import com.movie.api.storage.repository.UserMovieRecommendationRepository;
import com.movie.api.storage.repository.UserMovieScoreRepository;
import com.movie.api.storage.repository.UserMovieRepository;
import com.movie.api.storage.repository.WatchHistoryRepository;
import com.movie.api.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MovieService {
    private static final int RECOMMENDATION_SEED_LIMIT = 10;
    private static final int KNN_RECOMMENDATION_SEED_LIMIT = 20;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieItemMapper movieItemMapper;

    @Autowired
    private UserMovieRepository userMovieRepository;

    @Autowired
    private UserMovieScoreRepository userMovieScoreRepository;

    @Autowired
    private MovieSimilarityRepository movieSimilarityRepository;

    @Autowired
    private UserMovieRecommendationRepository userMovieRecommendationRepository;

    @Autowired
    private UserMovieService userMovieService;

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

    @Value("${recommendation.hybrid.model-version:hybrid-v1}")
    private String hybridRecommendationModelVersion;

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

    public List<MovieDto> getCachedSuggestedMovies(Long movieId) {
        // key -> movie:suggestion:{movieId}
        String key = redisService.buildKey("movie", "suggestion", movieId.toString());
        List<MovieDto> cachedMovies = redisService.get(key, new TypeReference<>() {
        });
        if (cachedMovies != null) {
            return cachedMovies;
        }

        Movie movie = movieRepository.findByIdAndStatus(movieId, BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Movie] Not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));

        List<MovieDto> suggestedMovies = movieMapper.fromEntityToMovieAutoCompleteDtoList(findSuggestedMovies(movie, 10));
        redisService.put(key, suggestedMovies, 5 * 60);
        return suggestedMovies;
    }

    public List<MovieDto> getCachedSuggestedMovies(Movie movie, int limit) {
        // key -> movie:suggestion:{movieId}
        int suggestionLimit = limit > 0 ? limit : 10;
        String key = redisService.buildKey("movie", "suggestion", movie.getId().toString(), String.valueOf(suggestionLimit));
        List<MovieDto> cachedMovies = redisService.get(key, new TypeReference<>() {
        });
        if (cachedMovies != null) {
            return cachedMovies;
        }

        List<MovieDto> suggestedMovies = movieMapper.fromEntityToMovieAutoCompleteDtoList(findSuggestedMovies(movie, suggestionLimit));
        redisService.put(key, suggestedMovies, 5 * 60);
        return suggestedMovies;
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

    public List<MovieDto> getRecommendationsForUser(Long userId, int limit) {
        int recommendationLimit = limit > 0 ? limit : 10;
        String key = redisService.buildKey(
                "movie",
                "recommendation",
                userId.toString()
        );
        List<MovieDto> cachedMovies = redisService.get(key, new TypeReference<>() {
        });
        if (cachedMovies != null) {
            return cachedMovies;
        }

        List<UserMovie> recentUserMovies = userMovieRepository.findByUserIdOrderByModifiedDateDesc(
                userId,
                PageRequest.of(0, RECOMMENDATION_SEED_LIMIT)
        );

        List<Long> seedMovieIds = recentUserMovies.stream()
                .filter(userMovie -> !Objects.equals(userMovie.getType(), BaseConstant.USER_MOVIE_TYPE_DISLIKED))
                .sorted(Comparator.comparingInt(this::getRecommendationSeedWeight).reversed())
                .map(UserMovie::getMovieId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Movie> seedMovies = findActiveMoviesByIds(seedMovieIds);

        Set<Long> excludedMovieIds = new LinkedHashSet<>();
        excludedMovieIds.addAll(userMovieRepository.findMovieIdsByUserIdAndTypeIn(
                userId,
                Arrays.asList(BaseConstant.USER_MOVIE_TYPE_WATCHED, BaseConstant.USER_MOVIE_TYPE_DISLIKED)
        ));
        excludedMovieIds.addAll(watchHistoryRepository.findAllWatchedMovieIds(userId));
        excludedMovieIds.addAll(seedMovieIds);
        excludedMovieIds = excludedMovieIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<MovieDto> recommendedMovies = findSuggestedMovies(seedMovies, excludedMovieIds, recommendationLimit);
        recommendedMovies = fillWithFallbackMovies(recommendedMovies, excludedMovieIds, recommendationLimit);
        redisService.put(key, recommendedMovies, 30 * 60); // cache 30 minutes
        return recommendedMovies;
    }

    @Transactional(readOnly = true)
    public Page<Movie> getHybridRecommendationsForUser(Long userId, Pageable pageable) {
        Pageable recommendationPageable = pageable != null ? pageable : PageRequest.of(0, 20);
        List<UserMovieRecommendation> recommendations = userMovieRecommendationRepository
                .findByUserIdAndModelVersionAndStatusOrderByScoreDesc(
                        userId,
                        hybridRecommendationModelVersion,
                        BaseConstant.STATUS_ACTIVE,
                        recommendationPageable
                );

        if (recommendations.isEmpty()) {
            log.info("Hybrid recommendation userId={}, modelVersion={}, recommendationCount=0, fallback=true",
                    userId,
                    hybridRecommendationModelVersion);
            return getFallbackRecommendationMovies(recommendationPageable);
        }

        List<Long> recommendedMovieIds = recommendations.stream()
                .map(UserMovieRecommendation::getMovieId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (recommendedMovieIds.isEmpty()) {
            log.info("Hybrid recommendation userId={}, modelVersion={}, recommendationCount={}, activeMovieCount=0, fallback=true",
                    userId,
                    hybridRecommendationModelVersion,
                    recommendations.size());
            return getFallbackRecommendationMovies(recommendationPageable);
        }

        List<Movie> movies = movieRepository.findAllByIdInAndStatus(recommendedMovieIds, BaseConstant.STATUS_ACTIVE);
        Map<Long, Movie> movieById = movies.stream()
                .collect(Collectors.toMap(Movie::getId, Function.identity(), (a, b) -> a));
        List<Movie> sortedMovies = recommendedMovieIds.stream()
                .map(movieById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (sortedMovies.isEmpty()) {
            log.info("Hybrid recommendation userId={}, modelVersion={}, recommendationCount={}, activeMovieCount=0, fallback=true",
                    userId,
                    hybridRecommendationModelVersion,
                    recommendations.size());
            return getFallbackRecommendationMovies(recommendationPageable);
        }

        log.info("Hybrid recommendation userId={}, modelVersion={}, recommendationCount={}, activeMovieCount={}, fallback=false",
                userId,
                hybridRecommendationModelVersion,
                recommendations.size(),
                sortedMovies.size());
        return new PageImpl<>(sortedMovies, recommendationPageable, sortedMovies.size());
    }

    @Transactional(readOnly = true)
    public Page<Movie> getRecommendationsByKnn(Long userId, Pageable pageable) {
        Pageable recommendationPageable = pageable != null ? pageable : PageRequest.of(0, 20);
        List<UserMovieScore> seedScores = userMovieScoreRepository.findPositiveScoresByUserId(
                userId,
                BaseConstant.USER_MOVIE_SCORE_MODEL_VERSION,
                PageRequest.of(0, KNN_RECOMMENDATION_SEED_LIMIT)
        );

        if (seedScores.isEmpty()) {
            return getFallbackRecommendationMovies(userId, recommendationPageable, 0, 0, 0);
        }

        Map<Long, Double> userScoreByMovieId = seedScores.stream()
                .filter(score -> score.getMovieId() != null && score.getScore() != null && score.getScore() > 0)
                .collect(Collectors.toMap(
                        UserMovieScore::getMovieId,
                        UserMovieScore::getScore,
                        Math::max,
                        LinkedHashMap::new
                ));

        if (userScoreByMovieId.isEmpty()) {
            return getFallbackRecommendationMovies(userId, recommendationPageable, seedScores.size(), 0, 0);
        }

        Set<Long> seedMovieIds = new LinkedHashSet<>(userScoreByMovieId.keySet());
        List<MovieSimilarity> similarities = movieSimilarityRepository.findByMovieIdsAndModelVersion(
                seedMovieIds,
                BaseConstant.MOVIE_SIMILARITY_MODEL_VERSION_ITEM_KNN
        );

        if (similarities.isEmpty()) {
            return getFallbackRecommendationMovies(userId, recommendationPageable, seedMovieIds.size(), 0, 0);
        }

        Set<Long> excludedMovieIds = userMovieScoreRepository.findByUserId(userId)
                .stream()
                .map(UserMovieScore::getMovieId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        excludedMovieIds.addAll(seedMovieIds);

        Map<Long, Double> candidateScoreMap = new HashMap<>();
        for (MovieSimilarity similarity : similarities) {
            Long sourceMovieId = similarity.getMovieId();
            Long candidateMovieId = similarity.getSimilarMovieId();
            Double similarityScore = similarity.getScore();

            if (sourceMovieId == null || candidateMovieId == null || excludedMovieIds.contains(candidateMovieId)) {
                continue;
            }

            Double userScore = userScoreByMovieId.get(sourceMovieId);
            if (userScore == null || userScore <= 0 || similarityScore == null || similarityScore <= 0) {
                continue;
            }

            double finalScore = Math.min(userScore, 10.0) * similarityScore;
            candidateScoreMap.merge(candidateMovieId, finalScore, Double::sum);
        }

        if (candidateScoreMap.isEmpty()) {
            return getFallbackRecommendationMovies(userId, recommendationPageable, seedMovieIds.size(), similarities.size(), 0);
        }

        List<Long> sortedCandidateMovieIds = candidateScoreMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        int fromIndex = (int) Math.min(recommendationPageable.getOffset(), sortedCandidateMovieIds.size());
        int toIndex = Math.min(fromIndex + recommendationPageable.getPageSize(), sortedCandidateMovieIds.size());
        List<Long> pageCandidateMovieIds = sortedCandidateMovieIds.subList(fromIndex, toIndex);

        List<Movie> movies = movieRepository.findAllByIdInAndStatus(pageCandidateMovieIds, BaseConstant.STATUS_ACTIVE);
        Map<Long, Movie> movieById = movies.stream()
                .collect(Collectors.toMap(Movie::getId, Function.identity(), (a, b) -> a));

        List<Movie> sortedMovies = pageCandidateMovieIds.stream()
                .map(movieById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (sortedMovies.isEmpty()) {
            return getFallbackRecommendationMovies(
                    userId,
                    recommendationPageable,
                    seedMovieIds.size(),
                    similarities.size(),
                    candidateScoreMap.size()
            );
        }

        log.info("KNN recommendation userId={}, seedCount={}, similarityCount={}, candidateCount={}, fallback=false",
                userId,
                seedMovieIds.size(),
                similarities.size(),
                candidateScoreMap.size());
        return new PageImpl<>(sortedMovies, recommendationPageable, sortedCandidateMovieIds.size());
    }

    private Page<Movie> getFallbackRecommendationMovies(Long userId,
                                                        Pageable pageable,
                                                        int seedCount,
                                                        int similarityCount,
                                                        int candidateCount) {
        log.info("KNN recommendation userId={}, seedCount={}, similarityCount={}, candidateCount={}, fallback=true",
                userId,
                seedCount,
                similarityCount,
                candidateCount);
        return getFallbackRecommendationMovies(pageable);
    }

    private Page<Movie> getFallbackRecommendationMovies(Pageable pageable) {
        Pageable fallbackPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return movieRepository.findActiveFallbackRecommendations(BaseConstant.STATUS_ACTIVE, fallbackPageable);
    }

    @Transactional
    public void applyReviewRatingPreference(Long userId, Long movieId, Integer rating) {
        if (userId == null || movieId == null) {
            return;
        }

        Integer userMovieType = resolveReviewUserMovieType(rating);
        if (userMovieType == null) {
            deleteReviewRatingPreference(userId, movieId);
            return;
        }

        userMovieService.saveSignalBySource(
                userId,
                movieId,
                null,
                userMovieType,
                BaseConstant.USER_MOVIE_SOURCE_REVIEW,
                Objects.equals(userMovieType, BaseConstant.USER_MOVIE_TYPE_DISLIKED) ? -5.0 : rating.doubleValue()
        );
    }

    @Transactional
    public void deleteReviewRatingPreference(Long userId, Long movieId) {
        userMovieService.deleteSignalBySource(userId, movieId, BaseConstant.USER_MOVIE_SOURCE_REVIEW);
    }

    @Transactional(readOnly = true)
    public RecentWatchedCategoryRecommendationDto getRecentWatchedCategoryRecommendationsForUser(Long userId, int movieLimit) {
        int recommendationMovieLimit = movieLimit > 0 ? movieLimit : 10;
        String key = redisService.buildKey(
                "movie",
                "recommendation",
                "recent-watched-category",
                "v3",
                userId.toString()
        );
        RecentWatchedCategoryRecommendationDto cached = redisService.get(key, new TypeReference<>() {
        });
        if (cached != null) {
            return cached;
        }

        List<Movie> recentWatchedMovies = watchHistoryRepository.findWatchedMoviesByUserOrderByDate(userId, PageRequest.of(0, 3));
        if (recentWatchedMovies.isEmpty()) {
            return null;
        }

        List<Long> excludedMovieIds = recentWatchedMovies
                .stream()
                .map(Movie::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (excludedMovieIds.isEmpty()) {
            excludedMovieIds.add(-1L);
        }

        Map<Long, Integer> countByCategoryId = new HashMap<>();
        Map<Long, Category> categoryById = new HashMap<>();
        Map<Long, Integer> firstMovieIndexByCategoryId = new HashMap<>();
        for (int movieIndex = 0; movieIndex < recentWatchedMovies.size(); movieIndex++) {
            Movie movie = recentWatchedMovies.get(movieIndex);
            if (movie.getCategories() == null) {
                continue;
            }
            Set<Long> seenCategoryIdInMovie = new HashSet<>();
            for (Category candidate : movie.getCategories()) {
                if (candidate == null || candidate.getId() == null) {
                    continue;
                }
                Long categoryId = candidate.getId();
                if (!seenCategoryIdInMovie.add(categoryId)) {
                    continue;
                }
                categoryById.putIfAbsent(categoryId, candidate);
                countByCategoryId.merge(categoryId, 1, Integer::sum);
                firstMovieIndexByCategoryId.putIfAbsent(categoryId, movieIndex);
            }
        }

        if (countByCategoryId.isEmpty()) {
            return null;
        }

        Long bestCategoryId = countByCategoryId.entrySet().stream()
                .max(Comparator.<Map.Entry<Long, Integer>>comparingInt(Map.Entry::getValue)
                        .thenComparingInt(e -> -firstMovieIndexByCategoryId.get(e.getKey())))
                .map(Map.Entry::getKey)
                .orElse(null);
        Category category = bestCategoryId != null ? categoryById.get(bestCategoryId) : null;
        if (category == null) {
            return null;
        }

        List<Movie> movies = movieRepository.findRecommendationByCategory(
                category.getId(),
                excludedMovieIds,
                PageRequest.of(0, recommendationMovieLimit)
        );
        if (movies == null || movies.isEmpty()) {
            return null;
        }

        RecentWatchedCategoryRecommendationDto dto = new RecentWatchedCategoryRecommendationDto();
        dto.setCategory(categoryMapper.entityToCategoryAutoCompleteDto(category));
        dto.setMovies(movieMapper.fromEntityToMovieAutoCompleteDtoList(movies));
        redisService.put(key, dto, 30 * 60); // cache 30 minutes
        return dto;
    }

    public List<MovieDto> findSuggestedMovies(List<Movie> movies, Set<Long> excludedMovieIds, int limit) {
        if (movies == null || movies.isEmpty()) {
            return Collections.emptyList();
        }

        int recommendationLimit = limit > 0 ? limit : 10;
        Set<Long> excludedIds = excludedMovieIds == null ? Collections.emptySet() : excludedMovieIds;
        Map<Long, MovieDto> recommendedMovies = new LinkedHashMap<>();
        int candidateLimit = Math.max(recommendationLimit * 3, recommendationLimit + Math.min(excludedIds.size(), 50));
        for (Movie movie : movies) {
            List<MovieDto> suggestedMovies = getCachedSuggestedMovies(movie, candidateLimit);
            for (MovieDto suggestedMovie : suggestedMovies) {
                if (suggestedMovie.getId() == null || excludedIds.contains(suggestedMovie.getId())) {
                    continue;
                }
                recommendedMovies.putIfAbsent(suggestedMovie.getId(), suggestedMovie);
                if (recommendedMovies.size() >= recommendationLimit) {
                    break;
                }
            }
            if (recommendedMovies.size() >= recommendationLimit) {
                break;
            }
        }
        return new ArrayList<>(recommendedMovies.values());
    }

    private List<MovieDto> fillWithFallbackMovies(List<MovieDto> recommendedMovies, Set<Long> excludedMovieIds, int limit) {
        int recommendationLimit = limit > 0 ? limit : 10;
        Map<Long, MovieDto> recommendedMovieById = new LinkedHashMap<>();
        Set<Long> fallbackExcludedMovieIds = new LinkedHashSet<>();
        if (excludedMovieIds != null) {
            fallbackExcludedMovieIds.addAll(excludedMovieIds);
        }

        for (MovieDto movie : recommendedMovies) {
            if (movie == null || movie.getId() == null || fallbackExcludedMovieIds.contains(movie.getId())) {
                continue;
            }
            recommendedMovieById.putIfAbsent(movie.getId(), movie);
            fallbackExcludedMovieIds.add(movie.getId());
            if (recommendedMovieById.size() >= recommendationLimit) {
                return new ArrayList<>(recommendedMovieById.values());
            }
        }

        List<Movie> featuredMovies = movieRepository.findFeaturedFallbackRecommendations(
                BaseConstant.STATUS_ACTIVE,
                normalizeExcludedMovieIds(fallbackExcludedMovieIds),
                PageRequest.of(0, recommendationLimit - recommendedMovieById.size())
        );
        appendFallbackMovies(recommendedMovieById, fallbackExcludedMovieIds, featuredMovies, recommendationLimit);

        if (recommendedMovieById.size() < recommendationLimit) {
            List<Movie> hotMovies = movieRepository.findHotFallbackRecommendations(
                    BaseConstant.STATUS_ACTIVE,
                    normalizeExcludedMovieIds(fallbackExcludedMovieIds),
                    PageRequest.of(0, recommendationLimit - recommendedMovieById.size())
            );
            appendFallbackMovies(recommendedMovieById, fallbackExcludedMovieIds, hotMovies, recommendationLimit);
        }

        return new ArrayList<>(recommendedMovieById.values());
    }

    private void appendFallbackMovies(Map<Long, MovieDto> recommendedMovieById, Set<Long> excludedMovieIds, List<Movie> movies, int limit) {
        List<MovieDto> movieDtos = movieMapper.fromEntityToMovieAutoCompleteDtoList(movies);
        for (MovieDto movie : movieDtos) {
            if (movie == null || movie.getId() == null || excludedMovieIds.contains(movie.getId())) {
                continue;
            }
            recommendedMovieById.putIfAbsent(movie.getId(), movie);
            excludedMovieIds.add(movie.getId());
            if (recommendedMovieById.size() >= limit) {
                return;
            }
        }
    }

    private List<Long> normalizeExcludedMovieIds(Collection<Long> excludedMovieIds) {
        if (excludedMovieIds == null || excludedMovieIds.isEmpty()) {
            return Collections.singletonList(-1L);
        }
        return excludedMovieIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ids -> ids.isEmpty() ? Collections.singletonList(-1L) : ids));
    }

    private int getRecommendationSeedWeight(UserMovie userMovie) {
        if (userMovie == null) {
            return 0;
        }
        if (Objects.equals(userMovie.getType(), BaseConstant.USER_MOVIE_TYPE_INTERESTED)) {
            return 3;
        }
        if (Objects.equals(userMovie.getType(), BaseConstant.USER_MOVIE_TYPE_WATCHED)) {
            return 2;
        }
        return 1;
    }

    private Integer resolveReviewUserMovieType(Integer rating) {
        if (rating == null) {
            return null;
        }
        if (rating >= 3) {
            return BaseConstant.USER_MOVIE_TYPE_REVIEW;
        }
        return BaseConstant.USER_MOVIE_TYPE_DISLIKED;
    }

    private List<Movie> findActiveMoviesByIds(List<Long> movieIds) {
        if (movieIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Movie> movieById = movieRepository.findAllByIdInAndStatus(movieIds, BaseConstant.STATUS_ACTIVE)
                .stream()
                .collect(Collectors.toMap(Movie::getId, Function.identity(), (a, b) -> a));

        return movieIds.stream()
                .map(movieById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
        if (!StringUtils.isNullOrEmpty(movie.getMetadata())) {
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
            MovieMetadataForm metadata = (!StringUtils.isNullOrEmpty(movie.getMetadata()))
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
