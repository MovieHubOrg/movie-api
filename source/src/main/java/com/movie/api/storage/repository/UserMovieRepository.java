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

    @Modifying
    @Transactional
    @Query("DELETE FROM UserMovie um WHERE um.source IN :sources")
    int deleteBySourceIn(@Param("sources") List<String> sources);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserMovie um WHERE um.userId = :userId AND um.movieId = :movieId AND um.type = :type AND um.source = :source")
    int deleteByUserIdAndMovieIdAndTypeAndSource(@Param("userId") Long userId,
                                                 @Param("movieId") Long movieId,
                                                 @Param("type") Integer type,
                                                 @Param("source") String source);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserMovie um WHERE um.userId = :userId AND um.movieId = :movieId AND um.source = :source")
    int deleteByUserIdAndMovieIdAndSource(@Param("userId") Long userId,
                                          @Param("movieId") Long movieId,
                                          @Param("source") String source);

    List<UserMovie> findByUserId(Long userId);

    List<UserMovie> findByUserIdAndType(Long userId, Integer type);

    List<UserMovie> findByUserIdAndMovieId(Long userId, Long movieId);

    List<UserMovie> findByUserIdOrderByModifiedDateDesc(Long userId, Pageable pageable);

    List<UserMovie> findByUserIdAndTypeOrderByModifiedDateDesc(Long userId, Integer type, Pageable pageable);

    @Query("SELECT um.movieId FROM UserMovie um WHERE um.userId = :userId AND um.type = :type")
    List<Long> findMovieIdsByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);

    @Query("SELECT um.movieId FROM UserMovie um WHERE um.userId = :userId AND um.type IN :types")
    List<Long> findMovieIdsByUserIdAndTypeIn(@Param("userId") Long userId, @Param("types") List<Integer> types);

    @Query("SELECT um.movieId FROM UserMovie um WHERE um.userId = :userId AND um.type = :type")
    List<Movie> findAllByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type, Pageable pageable);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    Optional<UserMovie> findFirstByUserIdAndMovieIdAndSourceOrderByModifiedDateDesc(Long userId, Long movieId, String source);

    Optional<UserMovie> findFirstByUserIdAndMovieIdAndTypeAndSourceOrderByModifiedDateDesc(Long userId, Long movieId, Integer type, String source);

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
