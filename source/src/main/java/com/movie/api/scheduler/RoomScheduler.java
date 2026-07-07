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

    // TODO: production schedule — run every 6 hours
     @Scheduled(cron = "0 0 */6 * * *")
//    @Scheduled(fixedDelay = 1000)
    public void deleteExpiredPendingRooms() {
        log.info("======> Start scheduler deleteExpiredPendingRooms");
        int deleted = roomService.deleteExpiredPendingRooms();
        log.info("======> End scheduler deleteExpiredPendingRooms, deleted {} room(s)", deleted);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldEndedRooms() {
        log.info("======> Start scheduler deleteOldEndedRooms");
        int deleted = roomService.deleteOldEndedRooms();
        log.info("======> End scheduler deleteOldEndedRooms, deleted {} room(s)", deleted);
    }
}
