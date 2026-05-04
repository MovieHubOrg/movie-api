package com.movie.api.storage.repository;

import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.UserMovie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserMovieRepository extends JpaRepository<UserMovie, Long>, JpaSpecificationExecutor<UserMovie> {
    @Modifying
    @Transactional
    @Query("DELETE FROM UserMovie um WHERE um.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserMovie um WHERE um.userId = :userId AND um.type = :type")
    void deleteByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);

    List<UserMovie> findByUserId(Long userId);

    List<UserMovie> findByUserIdAndTypeOrderByModifiedDateDesc(Long userId, Integer type, Pageable pageable);

    @Query("SELECT um.movieId FROM UserMovie um WHERE um.userId = :userId AND um.type = :type")
    List<Long> findMovieIdsByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);

    @Query("SELECT um.movieId FROM UserMovie um WHERE um.userId = :userId AND um.type = :type")
    List<Movie> findAllByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type, Pageable pageable);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    Optional<UserMovie> findByUserIdAndMovieId(Long userId, Long movieId);

    @Query("SELECT DISTINCT um.userId " +
            "FROM UserMovie um " +
            "JOIN Account a ON a.id = um.userId " +
            "WHERE um.movieId IN :movieIds " +
            "AND a.status = :accountStatus " +
            "AND a.kind = :accountKind")
    List<Long> findDistinctUserIdsByMovieIds(
            @Param("movieIds") List<Long> movieIds,
            @Param("accountStatus") Integer accountStatus,
            @Param("accountKind") Integer accountKind
    );
}
