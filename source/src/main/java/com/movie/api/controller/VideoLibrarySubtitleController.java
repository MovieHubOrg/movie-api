package com.movie.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.video.DeleteSubtitleForm;
import com.movie.api.dto.video.TranslateSubtitleData;
import com.movie.api.dto.video.VideoLibrarySubtitleDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.video.TranslateSubtitleForm;
import com.movie.api.form.video.UpdateVideoLibrarySubtitleForm;
import com.movie.api.mapper.VideoLibrarySubtitleMapper;
import com.movie.api.service.rabbit.RabbitService;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.criteria.VideoLibrarySubtitleCriteria;
import com.movie.api.storage.model.VideoLibrary;
import com.movie.api.storage.model.VideoLibrarySubtitle;
import com.movie.api.storage.repository.VideoLibraryRepository;
import com.movie.api.storage.repository.VideoLibrarySubtitleRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/video-library-subtitle")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class VideoLibrarySubtitleController extends ABasicController {
    private static final int VIDEO_LIBRARY_SUBTITLE_LIST_CACHE_TTL_SECONDS = 5 * 60;

    @Autowired
    private VideoLibrarySubtitleRepository videoLibrarySubtitleRepository;

    @Autowired
    private VideoLibraryRepository videoLibraryRepository;

    @Autowired
    private VideoLibrarySubtitleMapper videoLibrarySubtitleMapper;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RedisService redisService;

    @Value("${rabbitmq.app}")
    private String appName;

    @Value("${rabbitmq.convert.video.queue}")
    private String convertVideoQueue;

    @Value("${rabbitmq.streaming.queue}")
    private String streamingQueue;

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VID_L_U')")
    @Transactional
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateVideoLibrarySubtitleForm form) {
        VideoLibrarySubtitle subtitle = videoLibrarySubtitleRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Video Library Subtitle] Not found", ErrorCode.VIDEO_LIBRARY_SUBTITLE_ERROR_NOT_FOUND));

        VideoLibrary videoLibrary = subtitle.getVideoLibrary();

        if (Boolean.TRUE.equals(form.getIsDefault()) && !Boolean.TRUE.equals(subtitle.getIsDefault())) {
            videoLibrarySubtitleRepository.clearDefaultByVideoLibraryId(videoLibrary.getId());
        }

        videoLibrarySubtitleMapper.fromUpdateFormToEntity(form, subtitle);
        videoLibrarySubtitleRepository.save(subtitle);
        evictPublicSubtitleListCache(videoLibrary.getId());
        return makeSuccessResponse("Update video library subtitle success");
    }

    @PostMapping(value = "/translate", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VID_L_U')")
    @Transactional
    public ApiMessageDto<Void> translate(@Valid @RequestBody TranslateSubtitleForm form) {
        VideoLibrarySubtitle sourceSubtitle = videoLibrarySubtitleRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Video Library Subtitle] Not found", ErrorCode.VIDEO_LIBRARY_SUBTITLE_ERROR_NOT_FOUND));

        if (!BaseConstant.VIDEO_LIBRARY_STATE_READY.equals(sourceSubtitle.getState())) {
            throw new BadRequestException("[Video Library Subtitle] Source subtitle is not ready");
        }

        if (StringUtils.isBlank(sourceSubtitle.getFileUrl())) {
            throw new BadRequestException("[Video Library Subtitle] Source subtitle has no file URL");
        }

        VideoLibrary videoLibrary = sourceSubtitle.getVideoLibrary();

        if (videoLibrary.getServerConfig() == null) {
            throw new BadRequestException("Cannot translate subtitle because server config is null");
        }

        if (videoLibrarySubtitleRepository.findByVideoLibraryIdAndLanguage(videoLibrary.getId(), form.getLanguage()).isPresent()) {
            throw new BadRequestException("[Video Library Subtitle] Destination language already exists", ErrorCode.VIDEO_LIBRARY_SUBTITLE_ERROR_LANGUAGE_EXISTED);
        }

        VideoLibrarySubtitle newSubtitle = new VideoLibrarySubtitle();
        newSubtitle.setVideoLibrary(videoLibrary);
        newSubtitle.setLanguage(form.getLanguage());
        newSubtitle.setLabel(form.getLabel());
        newSubtitle.setState(BaseConstant.VIDEO_LIBRARY_STATE_PROCESSING);
        newSubtitle.setIsDefault(false);
        videoLibrarySubtitleRepository.save(newSubtitle);

        TranslateSubtitleData data = new TranslateSubtitleData();
        data.setVideoId(videoLibrary.getId());
        data.setSubtitleId(newSubtitle.getId());
        data.setSourceLang(sourceSubtitle.getLanguage());
        data.setDestLang(form.getLanguage());
        data.setFileUrl(sourceSubtitle.getFileUrl());

        String queueName = videoLibrary.getServerConfig().getServerNumber() + "_" + convertVideoQueue;
        rabbitService.handleSendMsg(appName, queueName, data, BaseConstant.CMD_TRANSLATE_SUBTITLE);
        return makeSuccessResponse("Translate subtitle request submitted");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VID_L_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        VideoLibrarySubtitle subtitle = videoLibrarySubtitleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Video Library Subtitle] Not found", ErrorCode.VIDEO_LIBRARY_SUBTITLE_ERROR_NOT_FOUND));
        VideoLibrary videoLibrary = subtitle.getVideoLibrary();
        Long videoLibraryId = videoLibrary.getId();

        if (BaseConstant.VIDEO_LIBRARY_STATE_PROCESSING.equals(subtitle.getState())) {
            throw new BadRequestException("Cannot delete video library subtitle processing");
        }

        if (videoLibrary.getServerConfig() == null) {
            throw new BadRequestException("Cannot delete video library subtitle because server config is null");
        }

        if (StringUtils.isNotBlank(subtitle.getFileUrl())) {
            DeleteSubtitleForm data = new DeleteSubtitleForm();
            data.setVideoId(videoLibraryId);
            data.setFileUrl(subtitle.getFileUrl());

            String queueName = videoLibrary.getServerConfig().getServerNumber() + "_" + streamingQueue;
            rabbitService.handleSendMsg(appName, queueName, data, BaseConstant.CMD_DELETE_SUBTITLE);
        }

        videoLibrarySubtitleRepository.delete(subtitle);
        evictPublicSubtitleListCache(videoLibraryId);
        return makeSuccessResponse("Delete video library subtitle success");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VID_L_L')")
    public ApiMessageDto<ResponseListDto<List<VideoLibrarySubtitleDto>>> adminList(VideoLibrarySubtitleCriteria criteria, Pageable pageable) {
        validateVideoLibraryId(criteria);
        videoLibraryRepository.findById(criteria.getVideoLibraryId())
                .orElseThrow(() -> new NotFoundException("[Video Library] Not found", ErrorCode.VIDEO_LIBRARY_ERROR_NOT_FOUND));

        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdDate").descending());
        Page<VideoLibrarySubtitle> page = videoLibrarySubtitleRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(page, videoLibrarySubtitleMapper::fromEntityToVideoLibrarySubtitleDtoList), "List video library subtitle success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<VideoLibrarySubtitleDto>>> list(VideoLibrarySubtitleCriteria criteria) {
        validateVideoLibraryId(criteria);
        criteria.setState(BaseConstant.VIDEO_LIBRARY_STATE_READY);

        videoLibraryRepository.findByIdAndStatus(criteria.getVideoLibraryId(), BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Video Library] Not found", ErrorCode.VIDEO_LIBRARY_ERROR_NOT_FOUND));

        String key = buildPublicSubtitleListCacheKey(criteria);
        ResponseListDto<List<VideoLibrarySubtitleDto>> cached = redisService.get(key, new TypeReference<>() {
        });
        if (cached != null) {
            return makeSuccessResponse(cached, "List video library subtitle success");
        }

        Pageable pageable = PageRequest.of(0, 1000);
        Page<VideoLibrarySubtitle> page = videoLibrarySubtitleRepository.findAll(criteria.getSpecification(), pageable);
        ResponseListDto<List<VideoLibrarySubtitleDto>> response = makeResponseListDto(page, videoLibrarySubtitleMapper::entityToVideoLibrarySubtitlePublicDtoList);
        redisService.put(key, response, VIDEO_LIBRARY_SUBTITLE_LIST_CACHE_TTL_SECONDS);
        return makeSuccessResponse(response, "List video library subtitle success");
    }

    private void validateVideoLibraryId(VideoLibrarySubtitleCriteria criteria) {
        if (criteria.getVideoLibraryId() == null) {
            throw new BadRequestException("[Video Library Subtitle] videoLibraryId cannot be null");
        }
    }

    private String buildPublicSubtitleListCacheKey(VideoLibrarySubtitleCriteria criteria) {
        return redisService.buildKey(
                "video-library-subtitle",
                "list",
                criteria.getVideoLibraryId().toString(),
                criteria.getState().toString(),
                StringUtils.defaultString(criteria.getLabel()),
                StringUtils.defaultString(criteria.getLanguage())
        );
    }

    private void evictPublicSubtitleListCache(Long videoLibraryId) {
        redisService.deleteByPrefix(redisService.buildKey("video-library-subtitle", "list", videoLibraryId.toString()));
    }
}
