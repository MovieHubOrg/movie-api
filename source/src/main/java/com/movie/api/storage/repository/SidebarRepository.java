package com.movie.api.storage.repository;

import com.movie.api.storage.model.Sidebar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SidebarRepository extends JpaRepository<Sidebar, Long>, JpaSpecificationExecutor<Sidebar> {
    Optional<Sidebar> findByIdAndActive(Long id, Boolean active);

    boolean existsByMovieIdAndActive(Long movieId, Boolean active);

    @Modifying
    @Transactional
    @Query("DELETE Sidebar s WHERE s.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT MAX(s.ordering) FROM Sidebar s")
    Optional<Integer> findMaxOrdering();
}
