package com.movie.api.storage.repository;

import com.movie.api.dto.reaction.VoteDto;
import com.movie.api.storage.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.totalLike = r.totalLike + 1 WHERE r.id = :id")
    void increaseTotalLike(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.totalLike = r.totalLike - 1 WHERE r.id = :id AND r.totalLike > 0")
    void decreaseTotalLike(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.totalDislike = r.totalDislike + 1 WHERE r.id = :id")
    void increaseTotalDislike(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.totalDislike = r.totalDislike - 1 WHERE r.id = :id AND r.totalDislike > 0")
    void decreaseTotalDislike(@Param("id") Long id);

    @Query("SELECT new com.movie.api.dto.reaction.VoteDto(rv.id, r.type) " +
            "FROM Review rv " +
            "JOIN Reaction r ON rv.id = r.reviewId " +
            "WHERE rv.movieId = :movieId AND r.userId = :userId")
    List<VoteDto> findVotesByMovieIdAndUserId(@Param("movieId") Long movieId, @Param("userId") Long userId);

    boolean existsByAuthorIdAndMovieId(Long authorId, Long movieId);

    Optional<Review> findByAuthorIdAndMovieId(Long authorId, Long movieId);
}
