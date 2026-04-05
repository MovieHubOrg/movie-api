package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.video.VideoLibraryDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.video.CreateVideoLibraryForm;
import com.movie.api.form.video.ExternalVideoLibraryForm;
import com.movie.api.form.video.UpdateVideoLibraryForm;
import com.movie.api.mapper.VideoLibraryMapper;
import com.movie.api.service.rabbit.RabbitService;
import com.movie.api.storage.criteria.VideoLibraryCriteria;
import com.movie.api.storage.model.VideoLibrary;
import com.movie.api.storage.repository.MovieItemRepository;
import com.movie.api.storage.repository.VideoLibraryRepository;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/video-library")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class VideoLibraryController extends ABasicController {
    @Autowired
    private VideoLibraryRepository videoLibraryRepository;

    @Autowired
    private VideoLibraryMapper videoLibraryMapper;

    @Autowired
    private MovieItemRepository movieItemRepository;

    @Value("${rabbitmq.app}")
    private String appName;

    @Value("${rabbitmq.convert.video.queue}")
    private String convertVideoQueue;

    @Value("${rabbitmq.streaming.queue}")
    private String streamingQueue;

    @Autowired
    private RabbitService rabbitService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VID_L_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateVideoLibraryForm form) {
        VideoLibrary videoLibrary = videoLibraryRepository.findFirstByName(form.getName()).orElse(null);
        if (videoLibrary != null) {
            throw new BadRequestException("[Video Library] Name existed", ErrorCode.VIDEO_LIBRARY_ERROR_NAME_EXISTED);
        }

        videoLibrary = videoLibraryMapper.fromCreateVideoLibraryFormToEntity(form);

        if (Objects.equals(videoLibrary.getSourceType(), BaseConstant.SOURCE_TYPE_EXTERNAL)) {
            videoLibrary.setState(BaseConstant.VIDEO_LIBRARY_STATE_READY);
            updateExternalSource(videoLibrary, form);
        } else {
            videoLibrary.setState(BaseConstant.VIDEO_LIBRARY_STATE_PROCESSING);
        }
        videoLibraryRepository.save(videoLibrary);

        // send to CONVERT_MEDIA_QUEUE to convert video internal
        if (Objects.equals(videoLibrary.getSourceType(), BaseConstant.SOURCE_TYPE_INTERNAL)) {
            VideoLibraryDto data = new VideoLibraryDto();
            data.setId(videoLibrary.getId());
            data.setContent(videoLibrary.getContent());
            rabbitService.handleSendMsg(
                    appName,
                    convertVideoQueue,
                    data,
                    BaseConstant.CMD_CONVERT_VIDEO,
                    null,
                    null,
                    null
            );
        }
        return makeSuccessResponse("Create videoLibrary success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VID_L_V')")
    public ApiMessageDto<VideoLibraryDto> get(@PathVariable("id") Long id) {
        VideoLibrary videoLibrary = videoLibraryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Video Library] Not found", ErrorCode.VIDEO_LIBRARY_ERROR_NOT_FOUND));
        return makeSuccessResponse(videoLibraryMapper.entityToVideoLibraryDto(videoLibrary), "Get video library success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VID_L_L')")
    public ApiMessageDto<ResponseListDto<List<VideoLibraryDto>>> list(VideoLibraryCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdDate").descending());

        Page<VideoLibrary> videoLibraries = videoLibraryRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(videoLibraries, videoLibraryMapper::fromEntityToVideoLibraryDtoList), "List video library success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<VideoLibraryDto>>> autoComplete(VideoLibraryCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdDate").descending());

        Page<VideoLibrary> videoLibraries = videoLibraryRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(videoLibraries, videoLibraryMapper::fromEntityToVideoLibraryAutoCompleteDtoList), "List video library success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VID_L_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateVideoLibraryForm form) {
        VideoLibrary videoLibrary = videoLibraryRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Video Library] Not found", ErrorCode.VIDEO_LIBRARY_ERROR_NOT_FOUND));

        if (!Objects.equals(videoLibrary.getName(), form.getName()) && videoLibraryRepository.existsByName(form.getName())) {
            throw new BadRequestException("[Video Library] Name existed", ErrorCode.VIDEO_LIBRARY_ERROR_NAME_EXISTED);
        }

        videoLibraryMapper.fromUpdateVideoLibraryFormToEntity(form, videoLibrary);

        if (Objects.equals(videoLibrary.getSourceType(), BaseConstant.SOURCE_TYPE_EXTERNAL)) {
            if (StringUtils.isNoneBlank(form.getContent())) {
                videoLibrary.setContent(form.getContent());
            }
            updateExternalSource(videoLibrary, form);
        }

        videoLibraryRepository.save(videoLibrary);
        return makeSuccessResponse("Update video library success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VID_L_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        VideoLibrary videoLibrary = videoLibraryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Video Library] Not found", ErrorCode.VIDEO_LIBRARY_ERROR_NOT_FOUND));

        // set video_id = null
        movieItemRepository.detachVideoFromMovieItem(videoLibrary.getId());

        if (videoLibrary.getServerConfig() == null) {
            throw new BadRequestException("Cannot delete video library because server config is null");
        }
        // send message to delete video
        VideoLibraryDto data = new VideoLibraryDto();
        data.setId(id);

        String queueName = videoLibrary.getServerConfig().getServerNumber() + "_" + streamingQueue;
        rabbitService.handleSendMsg(
                appName,
                queueName,
                data,
                BaseConstant.CMD_DELETE_VIDEO,
                null,
                null,
                null
        );

        videoLibraryRepository.delete(videoLibrary);
        return makeSuccessResponse("Delete video library success");
    }

    private void updateExternalSource(VideoLibrary videoLibrary, ExternalVideoLibraryForm form) {
        if (form.getDuration() != null) {
            long endOfVideo = videoLibrary.getOutroStart() != null
                    ? videoLibrary.getOutroStart()
                    : videoLibrary.getIntroEnd() != null
                    ? videoLibrary.getIntroEnd()
                    : 0L;
            if (form.getDuration() <= endOfVideo) {
                throw new BadRequestException("[Video Library] duration invalid", ErrorCode.VIDEO_LIBRARY_ERROR_DURATION_INVALID);
            }
            videoLibrary.setDuration(form.getDuration());
        }
        if (form.getVttUrl() != null) {
            videoLibrary.setVttUrl(form.getVttUrl());
        }
        if (form.getSpriteUrl() != null) {
            videoLibrary.setSpriteUrl(form.getSpriteUrl());
        }
    }
}
