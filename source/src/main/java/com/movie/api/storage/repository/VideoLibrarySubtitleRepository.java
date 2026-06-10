package com.movie.api.storage.repository;

import com.movie.api.storage.model.VideoLibrarySubtitle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VideoLibrarySubtitleRepository extends JpaRepository<VideoLibrarySubtitle, Long>, JpaSpecificationExecutor<VideoLibrarySubtitle> {

    Page<VideoLibrarySubtitle> findByVideoLibrary_Id(Long videoLibraryId, Pageable pageable);

    @Query("SELECT s FROM VideoLibrarySubtitle s WHERE s.videoLibrary.id = :videoLibraryId AND s.language = :language")
    Optional<VideoLibrarySubtitle> findByVideoLibraryIdAndLanguage(@Param("videoLibraryId") Long videoLibraryId, @Param("language") String language);

    @Modifying
    @Transactional
    @Query("UPDATE VideoLibrarySubtitle s SET s.isDefault = false WHERE s.videoLibrary.id = :videoLibraryId")
    void clearDefaultByVideoLibraryId(@Param("videoLibraryId") Long videoLibraryId);
}
