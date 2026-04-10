package com.movie.api.service;

import com.movie.api.service.mqtt.MqttOutboundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    private MqttOutboundService mqttOutboundService;

    @Value("${mqtt.topic.notification.out}")
    private String notificationTopicOut;

    public <T> void sendToApp(String app, String cmd, T data, Integer qos) {
        String topic = notificationTopicOut + "/" + app;
        mqttOutboundService.sendToClient(topic, cmd, data, qos);
    }
}
