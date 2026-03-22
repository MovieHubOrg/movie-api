package com.movie.api.storage.repository;

import com.movie.api.storage.model.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long>, JpaSpecificationExecutor<AppVersion> {
    Optional<AppVersion> findFirstByName(String name);

    @Query("SELECT av FROM AppVersion av WHERE av.isLatest = true")
    Optional<AppVersion> findLatest();

    boolean existsByName(String name);

    boolean existsByIdNotNull();

    @Modifying
    @Transactional
    @Query("UPDATE FROM AppVersion av SET av.isLatest = false WHERE av.isLatest = true")
    void resetLatest();
}
