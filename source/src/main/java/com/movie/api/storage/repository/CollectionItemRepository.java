package com.movie.api.storage.repository;

import com.movie.api.storage.model.CollectionItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CollectionItemRepository extends JpaRepository<CollectionItem, Long>, JpaSpecificationExecutor<CollectionItem> {
    @Transactional
    @Modifying
    @Query("DELETE FROM CollectionItem ci WHERE ci.collection.id = :collectionId")
    void deleteByCollectionId(@Param("collectionId") Long collectionId);

    @Transactional
    @Modifying
    @Query("DELETE FROM CollectionItem ci WHERE ci.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") Long movieId);

    boolean existsByCollectionIdAndMovieId(Long collectionId, Long movieId);

    int countByCollectionId(Long collectionId);

    @Query("SELECT MAX(ci.ordering) FROM CollectionItem ci WHERE ci.collection.id = :collectionId")
    Optional<Integer> findMaxOrdering(@Param("collectionId") Long collectionId);

    @Query("select ci.id from CollectionItem ci where ci.collection.id = :collectionId")
    List<Long> findMovieIdByCollectionId(@Param("collectionId") Long collectionId);
}
