package com.movie.api.controller;

import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.appVersion.AppVersionDto;
import com.movie.api.dto.appVersion.CheckAppVersionDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.appVersion.CreateAppVersionForm;
import com.movie.api.form.appVersion.UpdateAppVersionForm;
import com.movie.api.mapper.AppVersionMapper;
import com.movie.api.service.MediaService;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.criteria.AppVersionCriteria;
import com.movie.api.storage.model.AppVersion;
import com.movie.api.storage.repository.AppVersionRepository;
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
import java.util.Objects;

@RestController
@RequestMapping("/v1/app-version")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AppVersionController extends ABasicController {
    private static final int APP_VERSION_LATEST_CACHE_TTL = 5 * 60;

    @Autowired
    private AppVersionRepository appVersionRepository;

    @Autowired
    private AppVersionMapper appVersionMapper;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private RedisService redisService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_V_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateAppVersionForm form) {
        if (appVersionRepository.existsByName(form.getName())) {
            throw new BadRequestException("[App Version] name existed", ErrorCode.APP_VERSION_ERROR_NAME_EXISTED);
        }

        if (form.getIsLatest()) {
            appVersionRepository.resetLatest();
        } else if (!appVersionRepository.existsByIdNotNull()) {
            form.setIsLatest(true);
        }

        AppVersion appVersion = appVersionMapper.fromCreateAppVersionFormToEntity(form);
        appVersionRepository.save(appVersion);
        clearLatestAppVersionCache();
        return makeSuccessResponse("Create app version success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_V_V')")
    public ApiMessageDto<AppVersionDto> get(@PathVariable("id") Long id) {
        AppVersion appVersion = appVersionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[App Version] Not found", ErrorCode.APP_VERSION_ERROR_NOT_FOUND));
        return makeSuccessResponse(appVersionMapper.entityToAppVersionDto(appVersion), "Get app version success.");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_V_L')")
    public ApiMessageDto<ResponseListDto<List<AppVersionDto>>> list(AppVersionCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("createdDate")));
        Page<AppVersion> appVersions = appVersionRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(appVersions, appVersionMapper::fromEntityToAppVersionDtoList), "List app version success");
    }

    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<AppVersionDto> getLatest() {
        String key = buildLatestAppVersionCacheKey();
        AppVersionDto cached = redisService.get(key, AppVersionDto.class);
        if (cached != null) {
            return makeSuccessResponse(cached, "Get latest app version success.");
        }

        AppVersion appVersion = appVersionRepository.findLatest()
                .orElseThrow(() -> new NotFoundException("[App Version] Not found latest", ErrorCode.APP_VERSION_ERROR_NOT_FOUND));
        AppVersionDto response = appVersionMapper.entityToAppVersionPublicDto(appVersion);
        redisService.put(key, response, APP_VERSION_LATEST_CACHE_TTL);
        return makeSuccessResponse(response, "Get latest app version success.");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_V_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateAppVersionForm form) {
        AppVersion appVersion = appVersionRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[App Version] Not found", ErrorCode.APP_VERSION_ERROR_NOT_FOUND));

        if (!Objects.equals(form.getName(), appVersion.getName()) && appVersionRepository.existsByName(form.getName())) {
            throw new BadRequestException("[App Version] name existed", ErrorCode.APP_VERSION_ERROR_NAME_EXISTED);
        }

        if (appVersion.getIsLatest() && !form.getIsLatest()) {
            throw new BadRequestException("[App Version] not have latest version", ErrorCode.APP_VERSION_ERROR_NOT_HAVE_LATEST_VERSION);
        }

        if (!Objects.equals(form.getFilePath(), appVersion.getFilePath())) {
            mediaService.deleteFile(appVersion.getFilePath());
        }

        if (!appVersion.getIsLatest() && form.getIsLatest()) {
            appVersionRepository.resetLatest();
        }

        appVersionMapper.fromUpdateAppVersionFormToEntity(form, appVersion);
        appVersionRepository.save(appVersion);
        clearLatestAppVersionCache();

        return makeSuccessResponse("Update app version success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_V_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        AppVersion appVersion = appVersionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[App Version] Not found", ErrorCode.APP_VERSION_ERROR_NOT_FOUND));
        if (appVersion.getIsLatest()) {
            throw new BadRequestException("[App Version] not have latest version", ErrorCode.APP_VERSION_ERROR_NOT_HAVE_LATEST_VERSION);
        }

        mediaService.deleteFile(appVersion.getFilePath());
        appVersionRepository.delete(appVersion);
        clearLatestAppVersionCache();
        return makeSuccessResponse("Delete app version success.");
    }

    @GetMapping(value = "/check-version", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<CheckAppVersionDto> checkVersion(@RequestParam("name") String name) {
        AppVersion appVersion = appVersionRepository.findFirstByName(name)
                .orElseThrow(() -> new NotFoundException("[App Version] Not found latest", ErrorCode.APP_VERSION_ERROR_NOT_FOUND));

        AppVersion latestVersion = appVersionRepository.findLatest()
                .orElseThrow(() -> new NotFoundException("[App Version] Not found latest", ErrorCode.APP_VERSION_ERROR_NOT_FOUND));

        CheckAppVersionDto checkAppVersionDto = new CheckAppVersionDto();
        checkAppVersionDto.setUpdateRequired(!Objects.equals(appVersion.getId(), latestVersion.getId()));
        checkAppVersionDto.setForceUpdate(latestVersion.getForceUpdate());
        checkAppVersionDto.setLatestVersion(appVersionMapper.entityToAppVersionDto(latestVersion));
        return makeSuccessResponse(checkAppVersionDto, "Check app version success.");
    }

    private String buildLatestAppVersionCacheKey() {
        return redisService.buildKey("app-version", "latest");
    }

    private void clearLatestAppVersionCache() {
        redisService.delete(buildLatestAppVersionCacheKey());
    }
}
