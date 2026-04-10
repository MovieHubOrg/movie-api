package com.movie.api.service.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.form.rabbit.BaseSendMsgForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttOutboundService {
    @Autowired
    private MessageChannel mqttOutboundChannel;

    @Autowired
    private ObjectMapper objectMapper;

    public void send(String topic, String payload, int qos) {
        mqttOutboundChannel.send(
                MessageBuilder.withPayload(payload)
                        .setHeader(MqttHeaders.TOPIC, topic)
                        .setHeader(MqttHeaders.QOS, qos)
                        .build()
        );
    }

    public <T> void sendToClient(String topic, String cmd, T data, int qos) {
        BaseSendMsgForm<T> form = new BaseSendMsgForm<>();
        form.setCmd(cmd);
        form.setData(data);
        String msg;
        try {
            msg = objectMapper.writeValueAsString(form);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        send(topic, msg, qos);
    }
}
