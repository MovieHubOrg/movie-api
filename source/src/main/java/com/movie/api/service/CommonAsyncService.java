package com.movie.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.dto.oneSignal.OneSignalPushNotificationForm;
import com.movie.api.service.feign.FeignOneSignalService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CommonAsyncService {
    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FeignOneSignalService feignOneSignalService;

    @Value("${onesignal.mobile.app-id}")
    private String oneSignalAppId;

    @Value("${onesignal.mobile.api-key}")
    private String oneSignalApiKey;

    @Autowired
    @Qualifier("threadPoolExecutor")
    @Getter
    private TaskExecutor taskExecutor;

    @Async
    public void sendEmail(String email, String msg, String subject, boolean html) {

        Runnable task3 = () -> {
            try {
                emailService.sendEmail(email, msg, subject, html);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        };
        taskExecutor.execute(task3);
    }

    @Async
    public void postMessageToOneSignal(OneSignalPushNotificationForm oneSignalPushNotification) {
        Runnable task3 = () -> {
            try {
                oneSignalPushNotification.setAppId(oneSignalAppId);
                oneSignalPushNotification.setTargetChannel("push");
                log.warn("Send notify body: {}", objectMapper.writeValueAsString(oneSignalPushNotification));

                feignOneSignalService.sendNotification(
                        "Key " + oneSignalApiKey,
                        oneSignalPushNotification
                );
                log.warn("Send notification success");
            } catch (IOException t) {
                log.error(t.getMessage(), t);
            }
        };
        taskExecutor.execute(task3);
    }
}
