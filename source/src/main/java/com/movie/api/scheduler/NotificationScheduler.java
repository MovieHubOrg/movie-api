package com.movie.api.scheduler;

import com.movie.api.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationScheduler {
    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void processNotificationTemplate() {
        log.info("======> Start scheduler processNotificationTemplate");
        notificationService.processScheduledNotificationTemplates();
        log.info("======> End scheduler processNotificationTemplate");
    }
}
