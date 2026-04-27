package com.movie.api.storage.repository;

import com.movie.api.storage.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long>, JpaSpecificationExecutor<Participant> {
    Optional<Participant> findByIdAndStatus(Long id, Integer status);

    Optional<Participant> findByRoomIdAndUserId(Long roomId, Long userId);

    List<Participant> findAllByRoom_IdAndUser_IdInAndStatus(Long roomId, List<Long> userIds, Integer status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Participant p WHERE p.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") Long roomId);

    boolean existsByRoomIdAndRoleAndState(Long roomId, Integer role, Integer state);

    @Modifying
    @Transactional
    @Query("UPDATE Participant p SET p.state = :newState WHERE p.room.id = :roomId AND p.state = :currentState")
    void updateStateByRoomIdAndState(
            @Param("roomId") Long roomId,
            @Param("currentState") Integer currentState,
            @Param("newState") Integer newState
    );

    int countByRoomIdAndState(Long roomId, Integer state);
}
