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

import java.util.Optional;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long>, JpaSpecificationExecutor<PlaylistItem> {
    Page<PlaylistItem> findAllByPlaylistId(Long playlistId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE PlaylistItem pli WHERE pli.playlist.id = :playlistId")
    void deleteByPlaylistId(@Param("playlistId") Long playlistId);

    @Modifying
    @Transactional
    @Query("DELETE PlaylistItem pli WHERE pli.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

    boolean existsByPlaylistIdAndMovieId(Long playlistId, Long movieId);

    Optional<PlaylistItem> findByPlaylistIdAndMovieId(Long playlistId, Long movieId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PlaylistItem pi WHERE pi.playlist.id = :playlistId AND pi.movie.id = :movieId")
    void deleteByPlaylistIdAndMovieId(
            @Param("playlistId") Long playlistId,
            @Param("movieId") Long movieId
    );
}
