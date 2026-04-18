package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.notification.NotificationDto;
import com.movie.api.form.notification.UpdateReadNotificationForm;
import com.movie.api.form.notification.TestSendNotificationForm;
import com.movie.api.mapper.NotificationMapper;
import com.movie.api.service.NotificationService;
import com.movie.api.storage.criteria.NotificationCriteria;
import com.movie.api.storage.model.Notification;
import com.movie.api.storage.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/notification")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class NotificationController extends ABasicController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationMapper notificationMapper;

    @PostMapping(value = "/send-notification", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> send(@Valid @RequestBody TestSendNotificationForm form) {
        notificationService.createNotificationTemplate(form.getTitle(), form.getBody(), form.getType(), form.getTargetType(), form.getTargetValue(), form.getScheduleAt());
        return makeSuccessResponse("Create style success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NOTI_L')")
    public ApiMessageDto<ResponseListDto<List<NotificationDto>>> list(NotificationCriteria criteria, Pageable pageable) {
        criteria.setAccountId(getCurrentUser());
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.DESC, "id")));
        Page<Notification> notifications = notificationRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(notifications, notificationMapper::fromEntityToNotificationDtoList), "List notification success");
    }

    @PutMapping(value = "/update-read", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NOTI_U')")
    public ApiMessageDto<Void> updateRead(@Valid @RequestBody UpdateReadNotificationForm form) {
        List<Long> ids = form.getIds().stream().distinct().collect(Collectors.toList());
        List<Notification> notifications = notificationRepository.findAllByIdInAndAccountId(ids, getCurrentUser());
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
        return makeSuccessResponse("Update notification read success");
    }
}
