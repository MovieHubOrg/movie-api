package com.movie.api.storage.repository;

import com.movie.api.storage.model.UserMovieRecommendation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserMovieRecommendationRepository extends JpaRepository<UserMovieRecommendation, Long>, JpaSpecificationExecutor<UserMovieRecommendation> {
    List<UserMovieRecommendation> findByUserIdAndModelVersionOrderByScoreDesc(Long userId, String modelVersion);

    List<UserMovieRecommendation> findByUserIdAndModelVersionAndStatusOrderByScoreDesc(Long userId,
                                                                                       String modelVersion,
                                                                                       int status,
                                                                                       Pageable pageable);

    Optional<UserMovieRecommendation> findByUserIdAndMovieIdAndModelVersion(Long userId, Long movieId, String modelVersion);
}
