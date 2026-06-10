package com.movie.api.storage.repository;

import com.movie.api.storage.model.MovieSimilarity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface MovieSimilarityRepository extends JpaRepository<MovieSimilarity, Long>, JpaSpecificationExecutor<MovieSimilarity> {
    List<MovieSimilarity> findByMovieIdOrderByScoreDesc(Long movieId, Pageable pageable);

    @Query("SELECT ms FROM MovieSimilarity ms " +
            "WHERE ms.movieId IN :movieIds " +
            "AND ms.modelVersion = :modelVersion")
    List<MovieSimilarity> findByMovieIdsAndModelVersion(@Param("movieIds") Collection<Long> movieIds,
                                                        @Param("modelVersion") String modelVersion);

    @Modifying
    @Transactional
    @Query("DELETE FROM MovieSimilarity ms WHERE ms.modelVersion = :modelVersion")
    int deleteByModelVersion(@Param("modelVersion") String modelVersion);
}
