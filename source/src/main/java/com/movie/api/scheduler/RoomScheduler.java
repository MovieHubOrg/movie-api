package com.movie.api.scheduler;

import com.movie.api.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoomScheduler {
    @Autowired
    private RoomService roomService;

    @Scheduled(fixedDelay = 60000)
    public void endTimedOutRooms() {
        log.info("======> Start scheduler endTimedOutRooms");
        int endedRooms = roomService.endTimedOutRunningRooms();
        log.info("======> End scheduler endTimedOutRooms, ended {} room(s)", endedRooms);
    }
}
