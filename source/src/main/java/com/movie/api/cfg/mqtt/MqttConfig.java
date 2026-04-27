package com.movie.api.cfg.mqtt;

import com.movie.api.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@Slf4j
public class MqttConfig {
    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.topic.notification.in}")
    private String notificationTopicIn;

    @Value("${mqtt.topic.notification.out}")
    private String notificationTopicOut;

    @Value("${mqtt.topic.room.prefix}")
    private String roomTopicPrefix;

    @Autowired
    private RoomService roomService;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{broker});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true); // auto reconnect
        options.setCleanSession(false); // keep session
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);

        factory.setConnectionOptions(options);
        return factory;
    }

    // ===== SUBSCRIBER =====
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter inbound() {
        String roomWildcardTopic = roomTopicPrefix + "/+";

        log.info("===> MQTT subscribing to: [{}] and [{}]", notificationTopicIn, roomWildcardTopic);

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId + "in",
                        mqttClientFactory(),
                        notificationTopicIn,
                        roomWildcardTopic
                );

        adapter.setQos(0);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setOutputChannel(mqttInputChannel());

        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String payload = message.getPayload().toString();
            String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
            log.info("Received from topic [{}]: {}", topic, payload);
            try {
                String roomTopicPrefixWithSeparator = roomTopicPrefix.endsWith("/") ? roomTopicPrefix : roomTopicPrefix + "/";
                if (topic != null && topic.startsWith(roomTopicPrefixWithSeparator)) {
                    String roomIdValue = topic.substring(roomTopicPrefixWithSeparator.length());
                    Long roomId = Long.parseLong(roomIdValue);
                    roomService.handleRoomMessage(roomId, payload);
//                mqttInboundService.handleInbound(payload);
                }
            } catch (Exception e) {
                log.error("Error processing mqtt message: {}", e.getMessage(), e);
            }
        };
    }

    // ===== PUBLISHER =====
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(clientId + "out", mqttClientFactory());
        handler.setAsync(true);
        handler.setDefaultTopic(notificationTopicOut);
        handler.setDefaultQos(0);
        return handler;
    }
}
