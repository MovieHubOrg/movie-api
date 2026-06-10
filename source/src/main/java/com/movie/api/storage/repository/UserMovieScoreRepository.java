package com.movie.api.storage.repository;

import com.movie.api.storage.model.UserMovieScore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserMovieScoreRepository extends JpaRepository<UserMovieScore, Long>, JpaSpecificationExecutor<UserMovieScore> {
    @Modifying
    @Transactional
    @Query("DELETE FROM UserMovieScore ums WHERE ums.modelVersion = :modelVersion")
    int deleteByModelVersion(@Param("modelVersion") String modelVersion);

    @Query("SELECT ums FROM UserMovieScore ums WHERE ums.score > 0")
    List<UserMovieScore> findPositiveScores();

    @Query("SELECT ums FROM UserMovieScore ums " +
            "WHERE ums.userId = :userId " +
            "AND ums.score > 0 " +
            "AND ums.modelVersion = :modelVersion " +
            "ORDER BY ums.score DESC")
    List<UserMovieScore> findPositiveScoresByUserId(@Param("userId") Long userId,
                                                    @Param("modelVersion") String modelVersion,
                                                    Pageable pageable);

    List<UserMovieScore> findByUserId(Long userId);
}
