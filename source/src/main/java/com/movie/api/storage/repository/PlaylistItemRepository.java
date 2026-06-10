package com.movie.api.storage.repository;

import com.movie.api.storage.model.PlaylistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long>, JpaSpecificationExecutor<PlaylistItem> {
    Page<PlaylistItem> findAllByPlaylistId(Long playlistId, Pageable pageable);

    List<PlaylistItem> findByPlaylistId(Long playlistId);

    @Modifying
    @Transactional
    @Query("DELETE PlaylistItem pli WHERE pli.playlist.id = :playlistId")
    void deleteByPlaylistId(@Param("playlistId") Long playlistId);

    @Modifying
    @Transactional
    @Query("DELETE PlaylistItem pli WHERE pli.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

    boolean existsByPlaylistIdAndMovieId(Long playlistId, Long movieId);

    @Query("SELECT CASE WHEN COUNT(pi) > 0 THEN true ELSE false END " +
            "FROM PlaylistItem pi " +
            "WHERE pi.playlist.user.id = :userId " +
            "AND pi.movie.id = :movieId")
    boolean existsByUserIdAndMovieId(@Param("userId") Long userId, @Param("movieId") Long movieId);

    Optional<PlaylistItem> findByPlaylistIdAndMovieId(Long playlistId, Long movieId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PlaylistItem pi WHERE pi.playlist.id = :playlistId AND pi.movie.id = :movieId")
    void deleteByPlaylistIdAndMovieId(
            @Param("playlistId") Long playlistId,
            @Param("movieId") Long movieId
    );


    @Query("SELECT pi FROM PlaylistItem pi " +
            "WHERE pi.status = :status " +
            "AND pi.playlist IS NOT NULL " +
            "AND pi.playlist.user IS NOT NULL " +
            "AND pi.movie IS NOT NULL")
    List<PlaylistItem> findRecommendationSignals(@Param("status") Integer status);
}
