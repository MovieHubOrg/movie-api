package com.movie.api.storage.repository;

import com.movie.api.storage.model.VideoLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VideoLibraryRepository extends JpaRepository<VideoLibrary, Long>, JpaSpecificationExecutor<VideoLibrary> {
    Optional<VideoLibrary> findFirstByName(String name);

    boolean existsByName(String name);
}
