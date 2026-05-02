package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.movie.MovieNotificationDto;
import com.movie.api.dto.notification.NotificationDto;
import com.movie.api.dto.notification.NotificationSummaryDto;
import com.movie.api.dto.oneSignal.AdditionalData;
import com.movie.api.dto.oneSignal.Content;
import com.movie.api.dto.oneSignal.IncludeAliases;
import com.movie.api.dto.oneSignal.OneSignalPushNotificationForm;
import com.movie.api.form.notification.SendNotificationConfigForm;
import com.movie.api.form.notification.TestSendOneSignalForm;
import com.movie.api.form.notification.UpdateReadNotificationForm;
import com.movie.api.form.notification.TestSendNotificationForm;
import com.movie.api.mapper.MovieMapper;
import com.movie.api.mapper.NotificationMapper;
import com.movie.api.service.CommonAsyncService;
import com.movie.api.service.NotificationService;
import com.movie.api.storage.criteria.NotificationCriteria;
import com.movie.api.storage.id.IdGenerator;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.Notification;
import com.movie.api.storage.repository.NotificationRepository;
import com.movie.api.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Date;
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

    @Autowired
    private CommonAsyncService commonAsyncService;

    @Autowired
    private MovieMapper movieMapper;

    @ApiIgnore
    @PostMapping(value = "/send-notification", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> send(@Valid @RequestBody TestSendNotificationForm form) {
        Movie movie = movieMapper.fromCreateMovieFormToEntity(form.getMovie());
        movie.setId(new IdGenerator().nextId());
        movie.setSlug(StringUtils.slugify(form.getTitle()));
        MovieNotificationDto data = movieMapper.entityToMovieNotificationDto(movie);
        notificationService.createNotificationTemplate(form.getTitle(), BaseConstant.CMD_NEW_MOVIE, data, BaseConstant.NOTIFICATION_TYPE_MOVIE, BaseConstant.NOTIFICATION_TARGET_TYPE_APP, BaseConstant.APP_MOVIE, form.getScheduleAt());
//        notificationService.createNotificationTemplate(form.getTitle(), form.getCmd(), data, form.getType(), form.getTargetType(), form.getTargetValue(), form.getScheduleAt());
        return makeSuccessResponse("Test send notification success");
    }

    @ApiIgnore
    @PostMapping(value = "/send-one-signal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> sendOneSignal(@Valid @RequestBody TestSendOneSignalForm form) {
        OneSignalPushNotificationForm oneSignalForm = new OneSignalPushNotificationForm();

        Content headings = new Content();
        headings.setEn(form.getTitle());
        oneSignalForm.setHeadings(headings);

        Content contents = new Content();
        contents.setEn(form.getBody());
        oneSignalForm.setContents(contents);

        IncludeAliases includeAliases = new IncludeAliases();
        includeAliases.setExternalId(form.getAccountIds());
        oneSignalForm.setIncludeAliases(includeAliases);

        AdditionalData<String> additionalData = new AdditionalData<>();
        additionalData.setData(form.getData());
        oneSignalForm.setData(additionalData);

        oneSignalForm.setBigPicture(form.getBigPicture());
        commonAsyncService.postMessageToOneSignal(oneSignalForm);
        return makeSuccessResponse("Send OneSignal notification success");
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

    @GetMapping(value = "/count-unread", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NOTI_L')")
    public ApiMessageDto<NotificationSummaryDto> countUnread() {
        long totalUnread = notificationRepository.countUnreadByAccountId(getCurrentUser());
        return makeSuccessResponse(new NotificationSummaryDto(totalUnread), "Count unread notification success");
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

    @PutMapping(value = "/read-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NOTI_U')")
    public ApiMessageDto<Void> readAll() {
        notificationRepository.markAllUnreadAsReadByAccountId(getCurrentUser());
        return makeSuccessResponse("Read all notifications success");
    }
}
