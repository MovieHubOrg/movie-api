package com.movie.api.storage.repository;

import com.movie.api.storage.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ChatRepository extends JpaRepository<Chat, Long>, JpaSpecificationExecutor<Chat> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Chat c WHERE c.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);
}
