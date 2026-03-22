package com.movie.api.storage.repository;

import com.movie.api.storage.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long>, JpaSpecificationExecutor<Reaction> {
    Optional<Reaction> findFirstByCommentIdAndUserId(Long commentId, Long userId);

    Optional<Reaction> findFirstByReviewIdAndUserId(Long reviewId, Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reaction r WHERE r.commentId = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reaction r WHERE r.reviewId = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);
}
