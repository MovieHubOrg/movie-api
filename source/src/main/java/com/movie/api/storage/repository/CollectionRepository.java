package com.movie.api.storage.repository;

import com.movie.api.storage.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long>, JpaSpecificationExecutor<Collection> {
    boolean existsByName(String name);

    @Transactional
    @Modifying
    @Query("UPDATE Collection c " +
            "SET c.style = (SELECT s FROM Style s WHERE s.isDefault = true) " +
            "WHERE c.style.id = :styleId")
    void updateStyleToDefault(@Param("styleId") Long styleId);

    @Query("SELECT MAX(c.ordering) FROM Collection c WHERE c.type = :type")
    Optional<Integer> findMaxOrdering(@Param("type") Integer type);
}
