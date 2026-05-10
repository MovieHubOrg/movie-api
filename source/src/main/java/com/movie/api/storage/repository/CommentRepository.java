package com.movie.api.storage.repository;

import com.movie.api.dto.reaction.VoteDto;
import com.movie.api.storage.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Comment c SET c.totalChildren = c.totalChildren + 1 WHERE c.id = :id")
    void increaseTotalChild(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.totalChildren = c.totalChildren - 1 WHERE c.id = :id AND c.totalChildren > 0")
    void decreaseTotalChild(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.totalLike = c.totalLike + 1 WHERE c.id = :id")
    void increaseTotalLike(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.totalLike = c.totalLike - 1 WHERE c.id = :id AND c.totalLike > 0")
    void decreaseTotalLike(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.totalDislike = c.totalDislike + 1 WHERE c.id = :id")
    void increaseTotalDislike(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.totalDislike = c.totalDislike - 1 WHERE c.id = :id AND c.totalDislike > 0")
    void decreaseTotalDislike(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.parent.id = :parentId")
    void deleteByParentId(@Param("parentId") Long parentId);

    @Modifying
    @Transactional
    @Query(value = "DELETE c FROM db_comment c " +
            "JOIN db_movie_item mi ON c.movie_item_id = mi.id " +
            "WHERE mi.id = :movieItemId OR mi.parent_id = :movieItemId",
            nativeQuery = true)
    void deleteByMovieItemId(@Param("movieItemId") Long movieItemId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.movieId = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.status = :status WHERE c.parent.id = :parentId")
    void updateStatusByParentId(@Param("parentId") Long parentId, @Param("status") Integer status);

    @Query("SELECT new com.movie.api.dto.reaction.VoteDto(c.id, r.type) " +
            "FROM Comment c " +
            "JOIN Reaction r ON c.id = r.commentId " +
            "WHERE c.movieId = :movieId AND r.userId = :userId")
    List<VoteDto> findVotesByMovieIdAndUserId(@Param("movieId") Long movieId, @Param("userId") Long userId);

    @Query("select count(c) from Comment c " +
            "where c.status = :status " +
            "and (:fromDate is null or c.createdDate >= :fromDate) " +
            "and (:toDate is null or c.createdDate <= :toDate)")
    Long countByStatusAndCreatedDateBetween(@Param("status") Integer status,
                                            @Param("fromDate") Date fromDate,
                                            @Param("toDate") Date toDate);
}
