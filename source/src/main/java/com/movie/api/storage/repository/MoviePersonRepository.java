package com.movie.api.storage.repository;

import com.movie.api.storage.model.MoviePerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MoviePersonRepository extends JpaRepository<MoviePerson, Long>, JpaSpecificationExecutor<MoviePerson> {
    boolean existsByPersonId(Long id);

    @Modifying
    @Transactional
    @Query("DELETE MoviePerson mp where mp.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT MAX(mp.ordering) FROM MoviePerson mp WHERE mp.movie.id = :movieId AND mp.kind = :kind")
    Optional<Integer> findMaxOrdering(@Param("movieId") Long movieId, @Param("kind") Integer kind);
}
