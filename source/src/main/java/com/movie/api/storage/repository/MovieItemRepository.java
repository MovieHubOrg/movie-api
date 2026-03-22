package com.movie.api.storage.repository;

import com.movie.api.storage.model.MovieItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MovieItemRepository extends JpaRepository<MovieItem, Long>, JpaSpecificationExecutor<MovieItem> {

    Optional<MovieItem> findByIdAndStatus(Long id, Integer status);

    Optional<MovieItem> findByIdAndKindNot(Long id, Integer kind);

    @Query("SELECT mi FROM MovieItem mi LEFT JOIN FETCH mi.parent LEFT JOIN FETCH mi.video WHERE mi.movie.id = :movieId AND mi.status = :status")
    List<MovieItem> findByMovieIdAndStatusWithParent(@Param("movieId") Long movieId, @Param("status") Integer status);

    @Modifying
    @Transactional
    @Query("UPDATE MovieItem mi SET mi.video = null WHERE mi.video.id = :videoId")
    void detachVideoFromMovieItem(@Param("videoId") Long videoId);

    @Modifying
    @Transactional
    @Query("DELETE MovieItem mi WHERE mi.movie.id = :movieId AND mi.kind = :kind")
    void deleteByMovieIdAndKind(@Param("movieId") Long movieId, @Param("kind") Integer kind);

    @Query("SELECT mi.thumbnailUrl FROM MovieItem mi WHERE mi.movie.id = :movieId AND mi.thumbnailUrl IS NOT NULL ")
    List<String> findThumbnailsByMovieId(@Param("movieId") Long movieId);

    Long countByMovieIdAndKind(Long movieId, Integer kind);

    @Query("SELECT mi.id FROM MovieItem mi WHERE mi.parent.id = :parentId AND mi.kind != :kind")
    List<Long> findIdByParentIdAndKindNot(@Param("parentId") Long parentId, @Param("kind") Integer kind);

    @Query("SELECT MAX(mi.ordering) " +
            "FROM MovieItem mi " +
            "WHERE mi.movie.id = :movieId " +
            "AND (" +
            "   (:kind = 1 AND mi.parent IS NULL) " +
            "   OR (:kind != 1 AND mi.parent.id = :parentId)" +
            ")")
    Optional<Integer> findMaxOrdering(@Param("movieId") Long movieId, @Param("kind") Integer kind, @Param("parentId") Long parentId);

    boolean existsByMovieIdAndKindAndLabelAndParentIsNull(Long movieId, Integer kind, String label);

    boolean existsByMovieIdAndKindAndLabelAndParentId(Long movieId, Integer kind, String label, Long parentId);

    Optional<MovieItem> findFirstByMovieIdAndKindAndIdNotOrderByOrderingDesc(Long movieId, Integer kind, Long id);

    @Query("select count(mi) from MovieItem mi where mi.parent.id = :parentId and mi.kind = 2")
    Long countCurrentTotalEpisodes(@Param("parentId") Long parentId);

    @Modifying
    @Transactional
    @Query("UPDATE FROM MovieItem mi SET mi.isLatest = false WHERE mi.movie.id = :movieId AND mi.kind = :kind AND mi.isLatest = true")
    void resetLatest(@Param("movieId") Long movieId, @Param("kind") Integer kind);
}
