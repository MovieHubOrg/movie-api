package com.movie.api.storage.repository;

import com.movie.api.storage.model.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StyleRepository extends JpaRepository<Style, Long>, JpaSpecificationExecutor<Style> {
    boolean existsByType(Integer type);

    boolean existsByIdNotNull();

    @Modifying
    @Transactional
    @Query("UPDATE FROM Style s SET s.isDefault = false WHERE s.isDefault = true")
    void resetDefault();
}
