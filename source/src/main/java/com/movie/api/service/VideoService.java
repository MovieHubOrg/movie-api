package com.movie.api.service;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.video.VideoLibraryDto;
import com.movie.api.dto.video.VideoLibraryNotificationDto;
import com.movie.api.form.video.UpdateVideoForm;
import com.movie.api.mapper.VideoLibraryMapper;
import com.movie.api.storage.model.ServerConfig;
import com.movie.api.storage.model.VideoLibrary;
import com.movie.api.storage.repository.ServerConfigRepository;
import com.movie.api.storage.repository.VideoLibraryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class VideoService {
    @Autowired
    private VideoLibraryRepository videoLibraryRepository;

    @Autowired
    private VideoLibraryMapper videoLibraryMapper;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @Autowired
    private NotificationService notificationService;

    public void updateVideoLibrary(UpdateVideoForm form) {
        log.warn("Start updating video ID: {}", form.getId());
        log.warn(form.getContent());
        log.warn("Server number: {}", form.getServerNumber());

        ServerConfig serverConfig = serverConfigRepository.findByServerNumber(form.getServerNumber()).orElse(null);
        if (serverConfig == null) {
            log.warn("Server config not found for server number: {}", form.getServerNumber());
            return;
        }

        VideoLibrary videoLibrary = videoLibraryRepository.findById(form.getId()).orElse(null);
        if (videoLibrary == null) {
            log.warn("Video library not found for ID: {}", form.getId());
            return;
        }
        videoLibraryMapper.fromUpdateVideoFormToEntity(form, videoLibrary);
        videoLibrary.setServerConfig(serverConfig);
        videoLibrary = videoLibraryRepository.save(videoLibrary);
        log.warn("End updating video ID: {}", form.getId());

        VideoLibraryNotificationDto data = videoLibraryMapper.entityToVideoLibraryDtoNotification(videoLibrary);
        notificationService.sendToApp(BaseConstant.APP_CMS, BaseConstant.CMD_DONE_CONVERT_VIDEO, data, BaseConstant.MQTT_QOS_LEVEL_0);
        String title = String.format("Video \"%s\" đã xử lý xong", videoLibrary.getName());
        notificationService.createNotificationTemplate(title, BaseConstant.CMD_DONE_CONVERT_VIDEO, data, BaseConstant.NOTIFICATION_TYPE_CMS, BaseConstant.NOTIFICATION_TARGET_TYPE_APP, BaseConstant.APP_CMS, new Date());
    }
}
