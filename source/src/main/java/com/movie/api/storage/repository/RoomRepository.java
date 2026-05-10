package com.movie.api.storage.repository;

import com.movie.api.storage.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    Optional<Room> findByIdAndStatus(Long id, Integer status);

    Optional<Room> findFirstByHostIdAndState(Long hostId, Integer state);

    @Query("SELECT r FROM Room r " +
            "WHERE r.state = :state " +
            "AND (r.endTime <= :now OR (r.lastTimeOnline IS NOT NULL AND r.lastTimeOnline <= :hostInactiveBefore))")
    List<Room> findRunningRoomsToEnd(@Param("state") Integer state,
                                     @Param("now") Date now,
                                     @Param("hostInactiveBefore") Date hostInactiveBefore);

    @Query("select r.id from Room r where r.state = :state")
    List<Long> findIdsByState(@Param("state") Integer state);

    Optional<Room> findFirstByCodeAndStateNot(String code, Integer state);

    boolean existsByCodeAndStateNot(String code, Integer state);

    boolean existsByHostIdAndState(Long hostId, Integer state);

    @Modifying
    @Transactional
    @Query("UPDATE Room r SET r.state = :newState, r.endTime = :endTime WHERE r.id = :roomId AND r.state = :currentState")
    int updateStateAndEndTimeByIdAndState(@Param("roomId") Long roomId,
                                          @Param("currentState") Integer currentState,
                                          @Param("newState") Integer newState,
                                          @Param("endTime") Date endTime);
}
