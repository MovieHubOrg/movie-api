package com.movie.api.service;

import com.movie.api.constant.BaseConstant;
import com.movie.api.storage.model.*;
import com.movie.api.storage.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendationDataService {
    private static final int BATCH_SIZE = 1000;

    @Autowired
    private UserMovieRepository userMovieRepository;

    @Autowired
    private UserMovieScoreRepository userMovieScoreRepository;

    @Autowired
    private UserMovieService userMovieService;

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private PlaylistItemRepository playlistItemRepository;

    @Transactional
    public void rebuildUserMovieData() {
        List<String> generatedSources = Arrays.asList(
                BaseConstant.USER_MOVIE_SOURCE_WATCH_HISTORY,
                BaseConstant.USER_MOVIE_SOURCE_REVIEW,
                BaseConstant.USER_MOVIE_SOURCE_FAVORITE,
                BaseConstant.USER_MOVIE_SOURCE_PLAYLIST
        );
        int deletedCount = userMovieRepository.deleteBySourceIn(generatedSources);

        List<UserMovie> signals = new ArrayList<>();
        appendWatchHistorySignals(signals);
        appendWatchProgressSignals(signals);
        appendReviewSignals(signals);
        appendFavouriteSignals(signals);
        appendPlaylistSignals(signals);

        int insertedCount = saveUserMovieSignals(signals);
        log.warn("Rebuilt UserMovie data: deleted={}, inserted={}", deletedCount, insertedCount);
    }

    @Transactional
    public void rebuildUserMovieScores() {
        List<UserMovie> userMovies = userMovieRepository.findAll();
        Map<UserMovieKey, List<UserMovie>> signalsByUserMovie = userMovies.stream()
                .filter(signal -> signal.getUserId() != null && signal.getMovieId() != null)
                .collect(Collectors.groupingBy(
                        signal -> new UserMovieKey(signal.getUserId(), signal.getMovieId()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<UserMovieScore> scores = signalsByUserMovie.entrySet().stream()
                .map(entry -> createUserMovieScore(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        int deletedCount = userMovieScoreRepository.deleteByModelVersion(BaseConstant.USER_MOVIE_SCORE_MODEL_VERSION);
        int insertedCount = saveUserMovieScores(scores);
        log.warn("Rebuilt UserMovieScore data: deleted={}, inserted={}, modelVersion={}",
                deletedCount,
                insertedCount,
                BaseConstant.USER_MOVIE_SCORE_MODEL_VERSION);
    }

    private void appendWatchHistorySignals(List<UserMovie> signals) {
        for (WatchHistory watchHistory : watchHistoryRepository.findRecommendationSignals(BaseConstant.STATUS_ACTIVE)) {
            Long userId = watchHistory.getUser() != null ? watchHistory.getUser().getId() : null;
            Long movieId = watchHistory.getMovie() != null ? watchHistory.getMovie().getId() : null;
            if (userId == null || movieId == null) {
                continue;
            }

            Long movieItemId = watchHistory.getMovieItem() != null ? watchHistory.getMovieItem().getId() : null;
            signals.add(createUserMovie(
                    userId,
                    movieId,
                    movieItemId,
                    BaseConstant.USER_MOVIE_TYPE_WATCHED,
                    BaseConstant.USER_MOVIE_SOURCE_WATCH_HISTORY,
                    1.0
            ));
        }
    }

    private void appendWatchProgressSignals(List<UserMovie> signals) {
        Map<UserMovieKey, WatchHistory> latestProgressByUserMovie = new LinkedHashMap<>();
        for (WatchHistory watchHistory : watchHistoryRepository.findWatchProgressRecommendationSignals(BaseConstant.STATUS_ACTIVE)) {
            Long userId = watchHistory.getUser() != null ? watchHistory.getUser().getId() : null;
            Long movieId = watchHistory.getMovie() != null ? watchHistory.getMovie().getId() : null;
            if (userId == null || movieId == null) {
                continue;
            }

            UserMovieKey key = new UserMovieKey(userId, movieId);
            WatchHistory existing = latestProgressByUserMovie.get(key);
            if (existing == null || isAfter(watchHistory.getModifiedDate(), existing.getModifiedDate())) {
                latestProgressByUserMovie.put(key, watchHistory);
            }
        }

        for (Map.Entry<UserMovieKey, WatchHistory> entry : latestProgressByUserMovie.entrySet()) {
            WatchHistory watchHistory = entry.getValue();
            Long movieItemId = watchHistory.getMovieItem() != null ? watchHistory.getMovieItem().getId() : null;
            Double progressValue = userMovieService.calculateWatchProgressValue(
                    watchHistory.getLastWatchSeconds(),
                    resolveWatchEndSeconds(watchHistory.getMovieItem())
            );
            if (progressValue <= 0) {
                continue;
            }

            signals.add(createUserMovie(
                    entry.getKey().getUserId(),
                    entry.getKey().getMovieId(),
                    movieItemId,
                    BaseConstant.USER_MOVIE_TYPE_WATCH_PROGRESS,
                    BaseConstant.USER_MOVIE_SOURCE_WATCH_HISTORY,
                    progressValue
            ));
        }
    }

    private void appendReviewSignals(List<UserMovie> signals) {
        for (Review review : reviewRepository.findRecommendationSignals(BaseConstant.STATUS_ACTIVE)) {
            Long userId = review.getAuthor() != null ? review.getAuthor().getId() : null;
            if (userId == null || review.getMovieId() == null || review.getRate() == null) {
                continue;
            }

            Integer type = review.getRate() >= 3
                    ? BaseConstant.USER_MOVIE_TYPE_REVIEW
                    : BaseConstant.USER_MOVIE_TYPE_DISLIKED;
            Double value = review.getRate() >= 3 ? review.getRate().doubleValue() : -5.0;
            signals.add(createUserMovie(
                    userId,
                    review.getMovieId(),
                    null,
                    type,
                    BaseConstant.USER_MOVIE_SOURCE_REVIEW,
                    value
            ));
        }
    }

    private void appendFavouriteSignals(List<UserMovie> signals) {
        for (Favourite favourite : favouriteRepository.findRecommendationMovieSignals(BaseConstant.STATUS_ACTIVE, BaseConstant.FAVOURITE_TYPE_MOVIE)) {
            Long userId = favourite.getUser() != null ? favourite.getUser().getId() : null;
            Long movieId = favourite.getMovie() != null ? favourite.getMovie().getId() : null;
            if (userId == null || movieId == null) {
                continue;
            }

            signals.add(createUserMovie(
                    userId,
                    movieId,
                    null,
                    BaseConstant.USER_MOVIE_TYPE_FAVORITE,
                    BaseConstant.USER_MOVIE_SOURCE_FAVORITE,
                    1.0
            ));
        }
    }

    private void appendPlaylistSignals(List<UserMovie> signals) {
        for (PlaylistItem playlistItem : playlistItemRepository.findRecommendationSignals(BaseConstant.STATUS_ACTIVE)) {
            Long userId = playlistItem.getPlaylist() != null && playlistItem.getPlaylist().getUser() != null
                    ? playlistItem.getPlaylist().getUser().getId()
                    : null;
            Long movieId = playlistItem.getMovie() != null ? playlistItem.getMovie().getId() : null;
            if (userId == null || movieId == null) {
                continue;
            }

            signals.add(createUserMovie(
                    userId,
                    movieId,
                    null,
                    BaseConstant.USER_MOVIE_TYPE_PLAYLIST,
                    BaseConstant.USER_MOVIE_SOURCE_PLAYLIST,
                    1.0
            ));
        }
    }

    private boolean isAfter(Date current, Date existing) {
        if (current == null) {
            return false;
        }
        return existing == null || current.after(existing);
    }

    private Long resolveWatchEndSeconds(MovieItem movieItem) {
        if (movieItem == null || movieItem.getVideo() == null) {
            return null;
        }
        return movieItem.getVideo().getOutroStart() != null
                ? movieItem.getVideo().getOutroStart()
                : movieItem.getVideo().getDuration();
    }

    private UserMovie createUserMovie(Long userId, Long movieId, Long movieItemId, Integer type, String source, Double value) {
        UserMovie userMovie = new UserMovie();
        userMovie.setUserId(userId);
        userMovie.setMovieId(movieId);
        userMovie.setMovieItemId(movieItemId);
        userMovie.setType(type);
        userMovie.setSource(source);
        userMovie.setValue(value);
        return userMovie;
    }

    private UserMovieScore createUserMovieScore(UserMovieKey key, List<UserMovie> signals) {
        UserMovieScore score = new UserMovieScore();
        score.setUserId(key.getUserId());
        score.setMovieId(key.getMovieId());
        score.setScore(calculateScore(signals));
        score.setLastEventDate(resolveLastEventDate(signals));
        score.setModelVersion(BaseConstant.USER_MOVIE_SCORE_MODEL_VERSION);
        return score;
    }

    private Double calculateScore(List<UserMovie> signals) {
        boolean hasDisliked = signals.stream()
                .anyMatch(signal -> Objects.equals(signal.getType(), BaseConstant.USER_MOVIE_TYPE_DISLIKED));
        if (hasDisliked) {
            return -8.0;
        }

        double score = 0.0;
        for (UserMovie signal : signals) {
            Integer type = signal.getType();
            if (type == null) {
                continue;
            }
            if (Objects.equals(type, BaseConstant.USER_MOVIE_TYPE_SURVEY)) {
                score += 3.0;
            } else if (Objects.equals(type, BaseConstant.USER_MOVIE_TYPE_WATCHED)) {
                score += 5.0;
            } else if (Objects.equals(type, BaseConstant.USER_MOVIE_TYPE_FAVORITE)) {
                score += 6.0;
            } else if (Objects.equals(type, BaseConstant.USER_MOVIE_TYPE_PLAYLIST)) {
                score += 4.0;
            } else if (Objects.equals(type, BaseConstant.USER_MOVIE_TYPE_REVIEW)) {
                score += signal.getValue() != null ? signal.getValue() : 4.0;
            } else if (Objects.equals(type, BaseConstant.USER_MOVIE_TYPE_WATCH_PROGRESS)) {
                score += signal.getValue() != null ? Math.min(signal.getValue(), 3.0) : 0.0;
            }
        }
        return score > 0 ? Math.min(score, 10.0) : score;
    }

    private Long resolveLastEventDate(List<UserMovie> signals) {
        return signals.stream()
                .map(UserMovie::getModifiedDate)
                .filter(Objects::nonNull)
                .map(Date::getTime)
                .max(Long::compareTo)
                .orElse(null);
    }

    private int saveUserMovieSignals(List<UserMovie> signals) {
        for (int index = 0; index < signals.size(); index += BATCH_SIZE) {
            userMovieRepository.saveAll(signals.subList(index, Math.min(index + BATCH_SIZE, signals.size())));
        }
        return signals.size();
    }

    private int saveUserMovieScores(List<UserMovieScore> scores) {
        for (int index = 0; index < scores.size(); index += BATCH_SIZE) {
            userMovieScoreRepository.saveAll(scores.subList(index, Math.min(index + BATCH_SIZE, scores.size())));
        }
        return scores.size();
    }

    private static class UserMovieKey {
        private final Long userId;
        private final Long movieId;

        private UserMovieKey(Long userId, Long movieId) {
            this.userId = userId;
            this.movieId = movieId;
        }

        private Long getUserId() {
            return userId;
        }

        private Long getMovieId() {
            return movieId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UserMovieKey)) {
                return false;
            }
            UserMovieKey that = (UserMovieKey) o;
            return Objects.equals(userId, that.userId) && Objects.equals(movieId, that.movieId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, movieId);
        }
    }
}
