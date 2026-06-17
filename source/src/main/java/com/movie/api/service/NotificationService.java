package com.movie.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.comment.CommentNotificationDto;
import com.movie.api.dto.movie.MovieNotificationDto;
import com.movie.api.dto.movieItem.MovieItemNotificationDto;
import com.movie.api.dto.notification.NotificationDto;
import com.movie.api.dto.oneSignal.AdditionalData;
import com.movie.api.dto.oneSignal.Content;
import com.movie.api.dto.oneSignal.IncludeAliases;
import com.movie.api.dto.oneSignal.OneSignalPushNotificationForm;
import com.movie.api.dto.review.ReviewNotificationDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private CommonAsyncService commonAsyncService;

    public <T> void sendToApp(String app, String cmd, T data, Integer qos) {
        String topic = notificationTopicOut + "/" + app;
        mqttOutboundService.sendToClient(topic, cmd, data, qos);
    }

    public <T> void createNotificationTemplate(String title, String cmd, T body, Integer type, Integer targetType, String targetValue, Date scheduleAt) {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setTitle(title);
        notificationTemplate.setCmd(cmd);
        notificationTemplate.setBody(serializeNotificationBody(body));
        notificationTemplate.setType(type);
        notificationTemplate.setTargetType(targetType);
        notificationTemplate.setTargetValue(targetValue);
        notificationTemplate.setScheduleAt(scheduleAt);
        notificationTemplate.setStatus(BaseConstant.STATUS_PENDING);
        notificationTemplateRepository.save(notificationTemplate);
    }

    @Transactional
    public <T> void sendNotificationMessage(String title, String cmd, T body, Integer type, Integer targetType, String targetValue) {
        String notificationBody = serializeNotificationBody(body);
        List<Account> accounts = resolveTargetAccounts(targetType, targetValue);
        if (accounts.isEmpty()) {
            log.warn("No account found for notification cmd {}", cmd);
            return;
        }

        List<Notification> notifications = accounts.stream()
                .map(account -> buildNotification(title, cmd, notificationBody, type, account))
                .collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
        log.info("Created {} notifications for cmd {}", notifications.size(), cmd);

        SendNotificationForm sendNotificationForm = buildSendNotificationForm(title, cmd, notificationBody, type, targetType, targetValue, accounts);
        rabbitService.handleSendMsg(appName, updateVideoQueue, sendNotificationForm, BaseConstant.CMD_SEND_NOTIFICATION);
    }

    @Transactional
    public void processScheduledNotificationTemplates() {
        Date now = new Date();
        Pageable pageable = PageRequest.of(0, 50);
        List<NotificationTemplate> templates = notificationTemplateRepository.findAllByStatusAndScheduleAtLessThanEqual(BaseConstant.STATUS_PENDING, now, pageable);

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

            SendNotificationForm sendNotificationForm = buildSendNotificationForm(
                    template.getTitle(),
                    template.getCmd(),
                    template.getBody(),
                    template.getType(),
                    template.getTargetType(),
                    template.getTargetValue(),
                    accounts
            );
            rabbitService.handleSendMsg(appName, updateVideoQueue, sendNotificationForm, BaseConstant.CMD_SEND_NOTIFICATION);
        }
        notificationTemplateRepository.saveAll(templates);
    }

    public void deleteActiveNotificationTemplates() {
        notificationTemplateRepository.deleteByStatus(BaseConstant.STATUS_ACTIVE);
    }

    private Notification buildNotification(NotificationTemplate template, Account account) {
        Notification notification = new Notification();
        notification.setAccount(account);
        notification.setTitle(template.getTitle());
        notification.setCmd(template.getCmd());
        notification.setBody(template.getBody());
        notification.setType(template.getType());
        return notification;
    }

    private Notification buildNotification(String title, String cmd, String body, Integer type, Account account) {
        Notification notification = new Notification();
        notification.setAccount(account);
        notification.setTitle(title);
        notification.setCmd(cmd);
        notification.setBody(body);
        notification.setType(type);
        return notification;
    }

    private SendNotificationForm buildSendNotificationForm(String title, String cmd, String body, Integer type, Integer targetType, String targetValue, List<Account> accounts) {
        SendNotificationForm sendNotificationForm = new SendNotificationForm();
        sendNotificationForm.setTitle(title);
        sendNotificationForm.setCmd(cmd);
        sendNotificationForm.setBody(body);
        sendNotificationForm.setType(type);
        sendNotificationForm.setTargetType(targetType);
        sendNotificationForm.setTargetValue(targetValue);
        if (Objects.equals(targetType, BaseConstant.NOTIFICATION_TARGET_TYPE_ACCOUNT)) {
            sendNotificationForm.setAccountIds(accounts.stream().map(Account::getId).collect(Collectors.toList()));
        }
        enrichNotificationMessage(sendNotificationForm);
        return sendNotificationForm;
    }

    private void enrichNotificationMessage(SendNotificationForm sendNotificationForm) {
        if (Objects.equals(sendNotificationForm.getCmd(), BaseConstant.CMD_NEW_MOVIE)) {
            try {
                MovieNotificationDto movie = objectMapper.readValue(sendNotificationForm.getBody(), MovieNotificationDto.class);
                sendNotificationForm.setMessage(String.format("\"%s\" đã lên sóng - Xem ngay kẻo lỡ!", movie.getTitle()));
                sendNotificationForm.setImageUrl(BaseConstant.DOWNLOAD_MEDIA_API + movie.getThumbnailUrl());
            } catch (Exception e) {
                log.warn("Failed to parse movie data for notification message: {}", e.getMessage());
            }
        } else if (Objects.equals(sendNotificationForm.getCmd(), BaseConstant.CMD_NEW_MOVIE_ITEM)) {
            try {
                MovieItemNotificationDto movieItem = objectMapper.readValue(sendNotificationForm.getBody(), MovieItemNotificationDto.class);
                sendNotificationForm.setMessage(String.format("\"%s\" vừa có nội dung mới: %s - Xem ngay kẻo lỡ!", movieItem.getMovie().getTitle(), movieItem.getTitle()));
                String imageUrl = movieItem.getThumbnailUrl() != null ? movieItem.getThumbnailUrl() : movieItem.getMovie().getThumbnailUrl();
                sendNotificationForm.setImageUrl(BaseConstant.DOWNLOAD_MEDIA_API + imageUrl);
            } catch (Exception e) {
                log.warn("Failed to parse movie item data for notification message: {}", e.getMessage());
            }
        } else if (Objects.equals(sendNotificationForm.getCmd(), BaseConstant.CMD_REPLY_COMMENT)) {
            try {
                CommentNotificationDto comment = objectMapper.readValue(sendNotificationForm.getBody(), CommentNotificationDto.class);
                String message = String.format("%s đã trả lời bình luận của bạn: %s", comment.getAuthor().getFullName(), comment.getContent());
                sendNotificationForm.setMessage(message);
            } catch (Exception e) {
                log.warn("Failed to parse comment data for notification message: {}", e.getMessage());
            }
        } else if (Objects.equals(sendNotificationForm.getCmd(), BaseConstant.CMD_VOTE_COMMENT)) {
            try {
                CommentNotificationDto comment = objectMapper.readValue(sendNotificationForm.getBody(), CommentNotificationDto.class);
                String action = Objects.equals(comment.getReactionType(), BaseConstant.REACTION_TYPE_LIKE)
                        ? "đã thích"
                        : "đã không thích";
                String message = String.format("%s %s bình luận của bạn: %s", comment.getAuthor().getFullName(), action, comment.getContent());
                sendNotificationForm.setMessage(message);
            } catch (Exception e) {
                log.warn("Failed to parse comment vote data for notification message: {}", e.getMessage());
            }
        } else if (Objects.equals(sendNotificationForm.getCmd(), BaseConstant.CMD_VOTE_REVIEW)) {
            try {
                ReviewNotificationDto review = objectMapper.readValue(sendNotificationForm.getBody(), ReviewNotificationDto.class);
                String action = Objects.equals(review.getReactionType(), BaseConstant.REACTION_TYPE_LIKE)
                        ? "đã thích"
                        : "đã không thích";
                String message = String.format("%s %s đánh giá của bạn: %s", review.getAuthor().getFullName(), action, review.getContent());
                sendNotificationForm.setMessage(message);
            } catch (Exception e) {
                log.warn("Failed to parse review vote data for notification message: {}", e.getMessage());
            }
        } else if (Objects.equals(sendNotificationForm.getCmd(), BaseConstant.CMD_TOXIC_COMMENT_LOCKED)) {
            try {
                sendNotificationForm.setMessage("Bình luận của bạn đã bị ẩn do chứa nội dung không phù hợp");
            } catch (Exception e) {
                log.warn("Failed to parse toxic comment data for notification message: {}", e.getMessage());
            }
        }
    }

    private <T> String serializeNotificationBody(T body) {
        try {
            return body != null ? objectMapper.writeValueAsString(body) : null;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize notification body", e);
        }
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
        notificationDto.setCmd(form.getCmd());
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

        if (BaseConstant.ONE_SIGNAL_ALLOWED_CMD.contains(form.getCmd())) {
            sendOneSignalNotification(form);
        }

    }

    private void sendOneSignalNotification(SendNotificationForm form) {
        List<Long> accountIds = form.getAccountIds();
        if (accountIds == null || accountIds.isEmpty()) {
            log.warn("No accountIds provided for sending OneSignal notification");
            return;
        }

        List<String> externalIds = accountIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());

        OneSignalPushNotificationForm oneSignalForm = new OneSignalPushNotificationForm();
        Content headings = new Content();
        headings.setEn(form.getTitle());
        oneSignalForm.setHeadings(headings);

        Content contents = new Content();
        contents.setEn(form.getMessage());
        oneSignalForm.setContents(contents);

        IncludeAliases includeAliases = new IncludeAliases();
        includeAliases.setExternalId(externalIds);
        oneSignalForm.setIncludeAliases(includeAliases);

        AdditionalData<String> additionalData = new AdditionalData<>();
        additionalData.setTitle(form.getTitle());
        additionalData.setContent(form.getMessage());
        additionalData.setCmd(form.getCmd());
        additionalData.setData(form.getBody());
        oneSignalForm.setData(additionalData);

        oneSignalForm.setBigPicture(form.getImageUrl());
        commonAsyncService.postMessageToOneSignal(oneSignalForm);
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
