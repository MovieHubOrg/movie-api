package com.movie.api.storage.repository;

import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.WatchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long>, JpaSpecificationExecutor<WatchHistory> {
    Optional<WatchHistory> findByMovieItemIdAndUserId(Long movieItemId, Long userId);

    @Query("SELECT wh From WatchHistory wh " +
            "WHERE wh.movie.id = :movieId " +
            "AND wh.user.id = :userId " +
            "AND wh.movieItem IS NULL")
    Optional<WatchHistory> findWatchHistoryMovie(@Param("movieId") Long movieId, @Param("userId") Long userId);

    @Query("SELECT COUNT(wh) FROM WatchHistory wh " +
            "WHERE wh.movie.id = :movieId " +
            "AND wh.user.id = :userId " +
            "AND wh.isCompleted = true " +
            "AND wh.movieItem IS NOT NULL " +
            "AND wh.status = :status")
    Long countCompletedWatchHistory(@Param("movieId") Long movieId, @Param("userId") Long userId, @Param("status") Integer status);

    @Query(
            "SELECT wh FROM WatchHistory wh " +
                    "WHERE wh.user.id = :userId " +
                    "AND wh.isCompleted = false " +
                    "AND wh.status = 1 " +
                    "AND wh.movieItem IS NOT NULL " +
                    "AND wh.modifiedDate = ( " +
                    "    SELECT MAX(wh2.modifiedDate) " +
                    "    FROM WatchHistory wh2 " +
                    "    WHERE wh2.user.id = wh.user.id " +
                    "    AND wh2.movie.id = wh.movie.id " +
                    "    AND wh2.isCompleted = false " +
                    "    AND wh2.status = 1 " +
                    ") " +
                    "ORDER BY wh.modifiedDate DESC"
    )
    List<WatchHistory> findLatestInProgressGroupedByMovie(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM WatchHistory WHERE movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

    @Transactional
    @Modifying
    @Query("DELETE FROM WatchHistory WHERE user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM WatchHistory WHERE movieItem.id IN :movieItemIds")
    void deleteByMovieItemIds(@Param("movieItemIds") List<Long> movieItemIds);

    @Transactional
    @Modifying
    @Query("update WatchHistory wh set wh.status = :statusDelete where wh.user.id = :userId and wh.movie.id = :movieId")
    void softDeleteByUserIdAndMovieId(@Param("statusDelete") Integer statusDelete, @Param("userId") Long userId, @Param("movieId") Long movieId);

    @Query("SELECT movie FROM WatchHistory wh " +
            "JOIN wh.movie movie " +
            "WHERE wh.user.id = :userId " +
            "AND wh.movieItem IS NULL " +
            "AND (wh.isCompleted = true OR COALESCE(wh.timesWatched, 0) > 0) " +
            "AND wh.status = 1 " +
            "ORDER BY wh.modifiedDate DESC")
    List<Movie> findWatchedMoviesByUserOrderByDate(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT wh FROM WatchHistory wh " +
            "WHERE wh.status = :status " +
            "AND wh.user IS NOT NULL " +
            "AND wh.movie IS NOT NULL " +
            "AND (wh.isCompleted = true OR COALESCE(wh.timesWatched, 0) > 0)")
    List<WatchHistory> findRecommendationSignals(@Param("status") Integer status);

    @Query("SELECT wh FROM WatchHistory wh " +
            "WHERE wh.status = :status " +
            "AND wh.user IS NOT NULL " +
            "AND wh.movie IS NOT NULL " +
            "AND wh.movieItem IS NOT NULL " +
            "AND wh.lastWatchSeconds IS NOT NULL " +
            "AND wh.lastWatchSeconds > 0 " +
            "AND (wh.isCompleted = false OR wh.isCompleted IS NULL)")
    List<WatchHistory> findWatchProgressRecommendationSignals(@Param("status") Integer status);

    @Query("SELECT wh.movie FROM WatchHistory wh " +
            "WHERE wh.user.id = :userId " +
            "AND wh.movie IS NOT NULL " +
            "AND wh.status = 1 " +
            "AND wh.modifiedDate = ( " +
            "    SELECT MAX(wh2.modifiedDate) " +
            "    FROM WatchHistory wh2 " +
            "    WHERE wh2.user.id = wh.user.id " +
            "    AND wh2.movie.id = wh.movie.id " +
            "    AND wh2.status = 1 " +
            ") " +
            "ORDER BY wh.modifiedDate DESC")
    List<Movie> findRecentMoviesByUserOrderByDate(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT wh.movie.id FROM WatchHistory wh " +
            "WHERE wh.user.id = :userId " +
            "AND wh.movie IS NOT NULL " +
            "AND wh.status = 1")
    List<Long> findAllWatchedMovieIds(@Param("userId") Long userId);

    @Query("select coalesce(sum(wh.timesWatched), 0) from WatchHistory wh " +
            "where wh.status = :status " +
            "and wh.movieItem is null " +
            "and (:fromDate is null or wh.modifiedDate >= :fromDate) " +
            "and (:toDate is null or wh.modifiedDate <= :toDate)")
    Long sumTimesWatchedByModifiedDateBetween(@Param("status") Integer status,
                                              @Param("fromDate") Date fromDate,
                                              @Param("toDate") Date toDate);
}
