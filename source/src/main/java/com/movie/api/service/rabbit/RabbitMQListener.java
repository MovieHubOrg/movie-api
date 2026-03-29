package com.movie.api.service.rabbit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.video.VideoLibraryDto;
import com.movie.api.form.rabbit.BaseSendMsgForm;
import com.movie.api.form.sns.BaseSendSignalPayloadForm;
import com.movie.api.form.video.UpdateVideoForm;
import com.movie.api.service.SnsService;
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

    @Autowired
    private SnsService snsService;

    @RabbitListener(queues = "${rabbitmq.update.video.queue}")
    public void receiveMessage(String message) {
        try {
            BaseSendMsgForm<UpdateVideoForm> baseMessageForm = objectMapper.readValue(message, new TypeReference<>() {
            });
            System.out.println("======> Received message from " + updateVideoQueue + ": " + message);
            if (baseMessageForm.getCmd().equals(BaseConstant.CMD_DONE_CONVERT_VIDEO)) {
                log.warn("==> Processing update video");
                VideoLibraryDto videoLibrary = videoService.updateVideoLibrary(baseMessageForm.getData());
                if (videoLibrary == null) {
                    log.warn("==> Video not found for ID: {}", baseMessageForm.getData().getId());
                    return;
                }

                BaseSendSignalPayloadForm<VideoLibraryDto> signalPayload = new BaseSendSignalPayloadForm<>();
                signalPayload.setCmd(baseMessageForm.getCmd());
                signalPayload.setData(videoLibrary);

                // send notification for admin
                snsService.sendSignal(signalPayload, BaseConstant.ACCOUNT_KIND_ADMIN);

                // send notification for employee
                snsService.sendSignal(signalPayload, BaseConstant.ACCOUNT_KIND_EMPLOYEE);

                log.warn("==> DONE processing message");
            }
        } catch (Exception e) {
            log.error("Error processing received message: {}", e.getMessage(), e);
        }
    }
}
