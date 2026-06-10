package com.movie.api.service;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.video.VideoLibraryNotificationDto;
import com.movie.api.dto.video.VideoLibrarySubtitleNotificationDto;
import com.movie.api.form.video.DoneProcessSubtitleForm;
import com.movie.api.form.video.DoneTranslateSubtitleForm;
import com.movie.api.form.video.UpdateAudioForm;
import com.movie.api.form.video.UpdateVideoForm;
import com.movie.api.mapper.VideoLibraryMapper;
import com.movie.api.mapper.VideoLibrarySubtitleMapper;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.model.ServerConfig;
import com.movie.api.storage.model.VideoLibrary;
import com.movie.api.storage.model.VideoLibrarySubtitle;
import com.movie.api.storage.repository.ServerConfigRepository;
import com.movie.api.storage.repository.VideoLibraryRepository;
import com.movie.api.storage.repository.VideoLibrarySubtitleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class VideoService {
    @Autowired
    private VideoLibraryRepository videoLibraryRepository;

    @Autowired
    private VideoLibraryMapper videoLibraryMapper;

    @Autowired
    private VideoLibrarySubtitleMapper videoLibrarySubtitleMapper;

    @Autowired
    private VideoLibrarySubtitleRepository videoLibrarySubtitleRepository;

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RedisService redisService;

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
            log.error("Video library not found for ID: {}", form.getId());
            return;
        }
        videoLibraryMapper.fromUpdateVideoFormToEntity(form, videoLibrary);
        videoLibrary.setServerConfig(serverConfig);
        videoLibrary = videoLibraryRepository.save(videoLibrary);
        String title;
        if (Objects.equals(videoLibrary.getState(), BaseConstant.VIDEO_LIBRARY_STATE_ERROR)) {
            title = String.format("Video \"%s\" xử lý video lỗi", videoLibrary.getName());
        } else {
            title = String.format("Video \"%s\" đã xử lý video xong", videoLibrary.getName());
        }
        sendNotificationForVideoLibrary(videoLibrary, title);
        log.warn("End updating video ID: {}", form.getId());
    }

    public void updateAudioLibrary(UpdateAudioForm form) {
        log.warn("Start updating audio for video ID: {}", form.getVideoId());
        VideoLibrary videoLibrary = videoLibraryRepository.findById(form.getVideoId()).orElse(null);
        if (videoLibrary == null) {
            log.error("Video library not found for ID: {}", form.getVideoId());
            return;
        }

        videoLibrary.setAudioUrl(form.getAudioUrl());
        videoLibrary.setAudioState(form.getAudioState());
        if (form.getReason() != null) {
            videoLibrary.setReason(form.getReason());
        }
        videoLibraryRepository.save(videoLibrary);
        String title;
        if (Objects.equals(videoLibrary.getState(), BaseConstant.VIDEO_LIBRARY_STATE_ERROR)) {
            title = String.format("Không thể xử lý audio cho video \"%s\"", videoLibrary.getName());
        } else {
            title = String.format(
                    "Audio của video \"%s\" đã xử lý xong. Hệ thống đang tiếp tục xử lý phụ đề cho ngôn ngữ gốc, vui lòng chờ thêm một chút...",
                    videoLibrary.getName()
            );
        }
        sendNotificationForVideoLibrary(videoLibrary, title, BaseConstant.CMD_DONE_CONVERT_AUDIO);
        log.info("End updating audio for video ID: {}", form.getVideoId());
    }

    public void saveSubtitle(DoneProcessSubtitleForm form) {
        log.info("Start saving subtitle for video ID: {}, language: {}", form.getVideoId(), form.getLanguage());
        VideoLibrary videoLibrary = videoLibraryRepository.findById(form.getVideoId()).orElse(null);
        if (videoLibrary == null) {
            log.error("Video library not found for ID: {}", form.getVideoId());
            return;
        }

        if (Objects.equals(form.getState(), BaseConstant.VIDEO_LIBRARY_STATE_ERROR)) {
            log.error("Subtitle processing failed for video ID: {}, language: {}, reason: {}", form.getVideoId(), form.getLanguage(), form.getReason());
            videoLibrary.setReason(form.getReason());
            videoLibraryRepository.save(videoLibrary);

            String title = String.format("Không thể xử lý phụ đề cho video \"%s\"", videoLibrary.getName());
            sendNotificationForVideoLibrary(videoLibrary, title);
            return;
        }

        VideoLibrarySubtitle subtitle = videoLibrarySubtitleRepository
                .findByVideoLibraryIdAndLanguage(form.getVideoId(), form.getLanguage())
                .orElseGet(() -> {
                    VideoLibrarySubtitle newSubtitle = new VideoLibrarySubtitle();
                    newSubtitle.setVideoLibrary(videoLibrary);
                    newSubtitle.setLanguage(form.getLanguage());
                    newSubtitle.setLabel(form.getLanguage());
                    newSubtitle.setIsDefault(true);
                    return newSubtitle;
                });
        subtitle.setState(form.getState());
        subtitle.setFileUrl(form.getFileUrl());
        subtitle = videoLibrarySubtitleRepository.save(subtitle);

        String title = String.format("Phụ đề gốc của video \"%s\" đã xử lý xong", videoLibrary.getName());
        sendNotificationForVideoLibrarySubtitle(title, subtitle);

        log.info("End saving subtitle for video ID: {}, language: {}", form.getVideoId(), form.getLanguage());
    }

    public void updateTranslatedSubtitle(DoneTranslateSubtitleForm form) {
        log.warn("Start update translated subtitle ID: {}", form.getSubtitleId());
        VideoLibrarySubtitle subtitle = videoLibrarySubtitleRepository.findById(form.getSubtitleId()).orElse(null);
        if (subtitle == null) {
            log.info("Video library subtitle not found for ID: {}", form.getSubtitleId());
            return;
        }
        Long videoLibraryId = subtitle.getVideoLibrary().getId();
        subtitle.setFileUrl(form.getFileUrl());
        subtitle.setState(form.getState());
        videoLibrarySubtitleRepository.save(subtitle);
        evictPublicSubtitleListCache(videoLibraryId);

        if (Objects.equals(form.getState(), BaseConstant.VIDEO_LIBRARY_STATE_READY)) {
            VideoLibrary videoLibrary = videoLibraryRepository.findById(videoLibraryId).orElse(null);
            if (videoLibrary == null) {
                log.warn("Video library not found for translated subtitle ID: {}", form.getSubtitleId());
            } else {
                String subtitleName = subtitle.getLabel() != null ? subtitle.getLabel() : subtitle.getLanguage();
                String title = String.format("Phụ đề dịch %s của video \"%s\" đã xử lý xong", subtitleName, videoLibrary.getName());
                sendNotificationForVideoLibrarySubtitle(title, subtitle);
            }
        }
        log.info("End update translated subtitle ID: {}", form.getSubtitleId());
    }

    private void evictPublicSubtitleListCache(Long videoLibraryId) {
        redisService.deleteByPrefix(redisService.buildKey("video-library-subtitle", "list", videoLibraryId.toString()));
    }

    private void sendNotificationForVideoLibrary(VideoLibrary videoLibrary, String title) {
        sendNotificationForVideoLibrary(videoLibrary, title, BaseConstant.CMD_DONE_CONVERT_VIDEO);
    }

    private void sendNotificationForVideoLibrary(VideoLibrary videoLibrary, String title, String cmd) {
        VideoLibraryNotificationDto data = videoLibraryMapper.entityToVideoLibraryDtoNotification(videoLibrary);
        notificationService.sendNotificationMessage(title, cmd, data, BaseConstant.NOTIFICATION_TYPE_CMS, BaseConstant.NOTIFICATION_TARGET_TYPE_APP, BaseConstant.APP_CMS);
    }

    private void sendNotificationForVideoLibrarySubtitle(String title, VideoLibrarySubtitle subtitle) {
        VideoLibrarySubtitleNotificationDto data = videoLibrarySubtitleMapper.entityToVideoLibrarySubtitleNotificationDto(subtitle);
        notificationService.sendNotificationMessage(title, BaseConstant.CMD_DONE_PROCESS_SUBTITLE, data, BaseConstant.NOTIFICATION_TYPE_CMS, BaseConstant.NOTIFICATION_TARGET_TYPE_APP, BaseConstant.APP_CMS);
    }
}
