package com.movie.api.service.rabbit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.form.rabbit.BaseSendMsgForm;
import com.movie.api.form.user.AccountEventForm;
import com.movie.api.form.video.UpdateVideoForm;
import com.movie.api.service.AccountSyncService;
import com.movie.api.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMQListener {
    @Value("${rabbitmq.update.video.queue}")
    private String updateVideoQueue;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VideoService videoService;

    @Value("${rabbitmq.account.queue.movie}")
    private String accountMovieQueue;

    @Autowired
    private AccountSyncService accountSyncService;

    @RabbitListener(queues = "${rabbitmq.update.video.queue}")
    public void receiveMessage(String message) {
        try {
            BaseSendMsgForm<UpdateVideoForm> baseMessageForm = objectMapper.readValue(message, new TypeReference<>() {
            });
            System.out.println("======> Received message from " + updateVideoQueue + ": " + message);
            if (baseMessageForm.getCmd().equals(BaseConstant.CMD_DONE_CONVERT_VIDEO)) {
                log.warn("==> Processing update video");
                videoService.updateVideoLibrary(baseMessageForm.getData());
                log.warn("==> DONE processing message");
            }
        } catch (Exception e) {
            log.error("Error processing received message: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.queue.movie}")
    public void receiveMessageSyncAccount(String message) {
        try {
            BaseSendMsgForm<AccountEventForm> baseMessageForm = objectMapper
                    .readValue(message, new TypeReference<>() {
                    });

            log.info("======> Received account event from {}: cmd={}",
                    accountMovieQueue, baseMessageForm.getCmd());

            switch (baseMessageForm.getCmd()) {
                case BaseConstant.ACCOUNT_EVENT_CREATED:
                    log.info("==> Processing account created event");
                    accountSyncService.syncCreated(baseMessageForm.getData());
                    break;
                case BaseConstant.ACCOUNT_EVENT_UPDATED:
                    log.info("==> Processing account updated event");
                    accountSyncService.syncUpdated(baseMessageForm.getData());
                    break;
                case BaseConstant.ACCOUNT_EVENT_DELETED:
                    log.info("==> Processing account deleted event");
                    accountSyncService.syncDeleted(baseMessageForm.getData());
                    break;
                case BaseConstant.ACCOUNT_EVENT_STATUS_CHANGED:
                    log.info("==> Processing account status changed event");
                    accountSyncService.syncStatusChanged(baseMessageForm.getData());
                    break;
                default:
                    log.warn("Unknown account cmd: {}", baseMessageForm.getCmd());
            }

            log.info("======> DONE processing account event: cmd={}", baseMessageForm.getCmd());

        } catch (Exception e) {
            log.error("Error processing account event: {}", e.getMessage(), e);
        }
    }
}
