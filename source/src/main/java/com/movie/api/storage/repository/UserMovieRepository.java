package com.movie.api.storage.repository;

import com.movie.api.storage.model.UserMovie;
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

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    Optional<UserMovie> findByUserIdAndMovieId(Long userId, Long movieId);
}
