package com.movie.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.notification.NotificationDto;
import com.movie.api.form.notification.SendNotificationForm;
import com.movie.api.service.mqtt.MqttOutboundService;
import com.movie.api.service.rabbit.RabbitService;
import com.movie.api.storage.model.Account;
import com.movie.api.storage.model.Notification;
import com.movie.api.storage.model.NotificationTemplate;
import com.movie.api.storage.repository.AccountRepository;
import com.movie.api.storage.repository.NotificationRepository;
import com.movie.api.storage.repository.NotificationTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    private MqttOutboundService mqttOutboundService;

    @Value("${mqtt.topic.notification.out}")
    private String notificationTopicOut;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Value("${rabbitmq.app}")
    private String appName;

    @Value("${rabbitmq.update.video.queue}")
    private String updateVideoQueue;

    @Autowired
    private RabbitService rabbitService;

    public <T> void sendToApp(String app, String cmd, T data, Integer qos) {
        String topic = notificationTopicOut + "/" + app;
        mqttOutboundService.sendToClient(topic, cmd, data, qos);
    }

    public <T> NotificationTemplate createNotificationTemplate(String title, T body, Integer type, Integer targetType, String targetValue, Date scheduleAt) {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setTitle(title);
        try {
            notificationTemplate.setBody(body != null ? objectMapper.writeValueAsString(body) : null);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize notification body", e);
        }
        notificationTemplate.setType(type);
        notificationTemplate.setTargetType(targetType);
        notificationTemplate.setTargetValue(targetValue);
        notificationTemplate.setScheduleAt(scheduleAt);
        notificationTemplate.setStatus(BaseConstant.STATUS_PENDING);
        return notificationTemplateRepository.save(notificationTemplate);
    }

    @Transactional
    public void processScheduledNotificationTemplates() {
        Date now = new Date();
        List<NotificationTemplate> templates = notificationTemplateRepository.findAllByStatusAndScheduleAtLessThanEqual(BaseConstant.STATUS_PENDING, now);

        if (templates.isEmpty()) {
            return;
        }

        for (NotificationTemplate template : templates) {
            template.setStatus(BaseConstant.STATUS_ACTIVE);

            List<Account> accounts = resolveTargetAccounts(template.getTargetType(), template.getTargetValue());
            if (accounts.isEmpty()) {
                log.warn("No account found for notification template id {}", template.getId());
                continue;
            }

            List<Notification> notifications = accounts.stream()
                    .map(account -> buildNotification(template, account))
                    .collect(Collectors.toList());
            notificationRepository.saveAll(notifications);
            log.info("Created {} notifications from template id {}", notifications.size(), template.getId());

            SendNotificationForm sendNotificationForm = new SendNotificationForm();
            sendNotificationForm.setTitle(template.getTitle());
            sendNotificationForm.setBody(template.getBody());
            sendNotificationForm.setType(template.getType());
            sendNotificationForm.setTargetType(template.getTargetType());
            sendNotificationForm.setTargetValue(template.getTargetValue());
            if (Objects.equals(template.getTargetType(), BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT)) {
                sendNotificationForm.setAccountIds(accounts.stream().map(Account::getId).collect(Collectors.toList()));
            }

            rabbitService.handleSendMsg(
                    appName,
                    updateVideoQueue,
                    sendNotificationForm,
                    BaseConstant.CMD_SEND_NOTIFICATION,
                    null,
                    null,
                    null
            );
        }
        notificationTemplateRepository.saveAll(templates);
    }

    private Notification buildNotification(NotificationTemplate template, Account account) {
        Notification notification = new Notification();
        notification.setAccount(account);
        notification.setTitle(template.getTitle());
        notification.setBody(template.getBody());
        notification.setType(template.getType());
        return notification;
    }

    private List<Account> resolveTargetAccounts(Integer targetType, String targetValue) {
        if (Objects.equals(targetType, BaseConstant.NOTIFICATION_TARGET_TYPE_APP)) {
            List<Integer> kinds = new ArrayList<>();
            if (Objects.equals(targetValue, BaseConstant.APP_CMS)) {
                kinds.add(BaseConstant.ACCOUNT_KIND_ADMIN);
                kinds.add(BaseConstant.ACCOUNT_KIND_EMPLOYEE);
            } else if (Objects.equals(targetValue, BaseConstant.APP_MOVIE)) {
                kinds.add(BaseConstant.ACCOUNT_KIND_USER);
            } else {
                log.warn("Unsupported notification app targetValue {}", targetValue);
                return List.of();
            }
            return accountRepository.findAllByStatusAndKindIn(BaseConstant.STATUS_ACTIVE, kinds);
        }

        if (Objects.equals(targetType, BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT)) {
            List<Long> accountIds = parseAccountIds(targetValue);
            if (accountIds.isEmpty()) {
                return List.of();
            }
            return accountRepository.findAllByIdInAndStatus(accountIds, BaseConstant.STATUS_ACTIVE);
        }

        log.warn("Unsupported notification targetType {}", targetType);
        return List.of();
    }

    private List<Long> parseAccountIds(String targetValue) {
        if (targetValue == null || targetValue.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(targetValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public void sendNotification(SendNotificationForm form) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle(form.getTitle());
        notificationDto.setBody(form.getBody());
        notificationDto.setType(form.getType());
        if (Objects.equals(form.getTargetType(), BaseConstant.NOTIFICATION_TARGET_TYPE_APP)) {
            String targetApp = resolveTargetApp(form.getTargetValue());
            if (targetApp == null) {
                return;
            }
            sendToApp(targetApp, BaseConstant.CMD_SEND_NOTIFICATION, notificationDto, BaseConstant.MQTT_QOS_LEVEL_0);
        } else if (Objects.equals(form.getTargetType(), BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT)) {
            List<Long> accountIds = resolveAccountIds(form);
            if (accountIds.isEmpty()) {
                log.warn("No accountIds provided for sending notification");
                return;
            }
            List<Account> accounts = accountRepository.findAllByIdInAndStatus(accountIds, BaseConstant.STATUS_ACTIVE);
            if (accounts.isEmpty()) {
                log.warn("No active accounts found for provided accountIds");
                return;
            }
            accounts.forEach(account -> {
                mqttOutboundService.sendToClient(notificationTopicOut + "/" + account.getId(), BaseConstant.CMD_SEND_NOTIFICATION, notificationDto, BaseConstant.MQTT_QOS_LEVEL_0);
            });
        } else {
            log.warn("Unsupported targetType {} for sending notification", form.getTargetType());
        }
    }

    private String resolveTargetApp(String targetValue) {
        if (Objects.equals(targetValue, BaseConstant.APP_CMS) || Objects.equals(targetValue, BaseConstant.APP_MOVIE)) {
            return targetValue;
        }
        log.warn("Unsupported targetValue {} for app notification", targetValue);
        return null;
    }

    private List<Long> resolveAccountIds(SendNotificationForm form) {
        if (form.getAccountIds() != null && !form.getAccountIds().isEmpty()) {
            return form.getAccountIds();
        }
        return parseAccountIds(form.getTargetValue());
    }
}
