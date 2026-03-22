package com.movie.api.storage.repository;

import com.movie.api.storage.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long>, JpaSpecificationExecutor<Playlist> {
    Optional<Playlist> findByIdAndUserId(Long id, Long userId);

    List<Playlist> findByUserIdOrderByCreatedDateDesc(Long userId);

    @Query("SELECT DISTINCT pl.id FROM Playlist pl " +
            "JOIN PlaylistItem pli ON pl.id = pli.playlist.id " +
            "WHERE pl.user.id = :userId AND pli.movie.id = :movieId")
    List<Long> findByMovieIdAndUserId(@Param("movieId") Long movieId, @Param("userId") Long userId);

    @Query("SELECT DISTINCT pl.id FROM Playlist pl " +
            "JOIN PlaylistItem pli ON pl.id = pli.playlist.id " +
            "WHERE pli.movie.id = :movieId")
    List<Long> findByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT pl FROM Playlist pl WHERE pl.id IN :ids AND pl.user.id = :userId")
    List<Playlist> findAllByIdInAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Playlist p SET p.totalMovie = :total WHERE p.id = :playlistId")
    void updateTotalMovie(@Param("playlistId") Long playlistId, @Param("total") int total);

    @Modifying
    @Transactional
    @Query("UPDATE Playlist p SET p.totalMovie = " +
            "(SELECT COUNT(pi) FROM PlaylistItem pi WHERE pi.playlist.id = p.id) " +
            "WHERE p.id IN :ids")
    void recalculateTotalMovieByIdIn(@Param("ids") List<Long> ids);

    long countByUserId(Long userId);
}
