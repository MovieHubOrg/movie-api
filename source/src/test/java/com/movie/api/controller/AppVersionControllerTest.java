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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppVersionControllerTest {

    @Mock
    private AppVersionRepository appVersionRepository;

    @Mock
    private AppVersionMapper appVersionMapper;

    @Mock
    private MediaService mediaService;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AppVersionController appVersionController;

    // ---------- create ----------

    @Test
    void create_whenNameExists_throwsBadRequestException() {
        CreateAppVersionForm form = new CreateAppVersionForm();
        form.setName("1.0.0");

        when(appVersionRepository.existsByName("1.0.0")).thenReturn(true);

        assertThatThrownBy(() -> appVersionController.create(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NAME_EXISTED);
    }

    @Test
    void create_whenIsLatestTrue_resetsLatestAndSaves() {
        CreateAppVersionForm form = new CreateAppVersionForm();
        form.setName("1.0.1");
        form.setIsLatest(true);

        AppVersion mapped = new AppVersion();
        when(appVersionRepository.existsByName("1.0.1")).thenReturn(false);
        when(appVersionMapper.fromCreateAppVersionFormToEntity(form)).thenReturn(mapped);
        when(redisService.buildKey("app-version", "latest")).thenReturn("app-version::latest");

        ApiMessageDto<Void> response = appVersionController.create(form);

        assertThat(response.getResult()).isTrue();
        verify(appVersionRepository, times(1)).resetLatest();
        verify(appVersionRepository, never()).existsByIdNotNull();
        verify(appVersionRepository, times(1)).save(mapped);
        verify(redisService, times(1)).delete("app-version::latest");
    }

    @Test
    void create_whenNotLatestAndNoExistingVersions_forcesFirstVersionAsLatest() {
        CreateAppVersionForm form = new CreateAppVersionForm();
        form.setName("1.0.2");
        form.setIsLatest(false);

        AppVersion mapped = new AppVersion();
        when(appVersionRepository.existsByName("1.0.2")).thenReturn(false);
        when(appVersionRepository.existsByIdNotNull()).thenReturn(false);
        when(appVersionMapper.fromCreateAppVersionFormToEntity(form)).thenReturn(mapped);
        when(redisService.buildKey("app-version", "latest")).thenReturn("app-version::latest");

        ApiMessageDto<Void> response = appVersionController.create(form);

        assertThat(response.getResult()).isTrue();
        assertThat(form.getIsLatest()).isTrue();
        verify(appVersionRepository, never()).resetLatest();
        verify(appVersionRepository, times(1)).save(mapped);
    }

    @Test
    void create_whenNotLatestButVersionsExist_doesNotForceLatest() {
        CreateAppVersionForm form = new CreateAppVersionForm();
        form.setName("1.0.3");
        form.setIsLatest(false);

        AppVersion mapped = new AppVersion();
        when(appVersionRepository.existsByName("1.0.3")).thenReturn(false);
        when(appVersionRepository.existsByIdNotNull()).thenReturn(true);
        when(appVersionMapper.fromCreateAppVersionFormToEntity(form)).thenReturn(mapped);
        when(redisService.buildKey("app-version", "latest")).thenReturn("app-version::latest");

        ApiMessageDto<Void> response = appVersionController.create(form);

        assertThat(response.getResult()).isTrue();
        assertThat(form.getIsLatest()).isFalse();
        verify(appVersionRepository, never()).resetLatest();
    }

    // ---------- get ----------

    @Test
    void get_whenFound_returnsSuccessResponse() {
        AppVersion appVersion = new AppVersion();
        appVersion.setId(1L);
        AppVersionDto dto = new AppVersionDto();

        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(appVersion));
        when(appVersionMapper.entityToAppVersionDto(appVersion)).thenReturn(dto);

        ApiMessageDto<AppVersionDto> response = appVersionController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(appVersionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appVersionController.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NOT_FOUND);
    }

    // ---------- list ----------

    @Test
    void list_returnsSuccessResponseWrappingList() {
        AppVersionCriteria criteria = new AppVersionCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        AppVersion appVersion = new AppVersion();
        appVersion.setId(1L);
        Page<AppVersion> page = new PageImpl<>(Collections.singletonList(appVersion));
        List<AppVersionDto> dtoList = Collections.singletonList(new AppVersionDto());

        when(appVersionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(appVersionMapper.fromEntityToAppVersionDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<AppVersionDto>>> response = appVersionController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(response.getData().getTotalElements()).isEqualTo(1);
    }

    // ---------- getLatest ----------

    @Test
    void getLatest_whenCached_returnsCachedValue() {
        when(redisService.buildKey("app-version", "latest")).thenReturn("app-version::latest");
        AppVersionDto cached = new AppVersionDto();
        when(redisService.get(eq("app-version::latest"), eq(AppVersionDto.class))).thenReturn(cached);

        ApiMessageDto<AppVersionDto> response = appVersionController.getLatest();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(cached);
    }

    @Test
    void getLatest_whenNotCachedAndFound_fetchesAndCaches() {
        when(redisService.buildKey("app-version", "latest")).thenReturn("app-version::latest");
        when(redisService.get(eq("app-version::latest"), eq(AppVersionDto.class))).thenReturn(null);

        AppVersion appVersion = new AppVersion();
        appVersion.setId(1L);
        AppVersionDto dto = new AppVersionDto();
        when(appVersionRepository.findLatest()).thenReturn(Optional.of(appVersion));
        when(appVersionMapper.entityToAppVersionPublicDto(appVersion)).thenReturn(dto);

        ApiMessageDto<AppVersionDto> response = appVersionController.getLatest();

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
        verify(redisService, times(1)).put(eq("app-version::latest"), eq(dto), any(Integer.class));
    }

    @Test
    void getLatest_whenNotCachedAndNotFound_throwsNotFoundException() {
        when(redisService.buildKey("app-version", "latest")).thenReturn("app-version::latest");
        when(redisService.get(eq("app-version::latest"), eq(AppVersionDto.class))).thenReturn(null);
        when(appVersionRepository.findLatest()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appVersionController.getLatest())
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NOT_FOUND);
    }

    // ---------- update ----------

    @Test
    void update_whenIdNotFound_throwsNotFoundException() {
        UpdateAppVersionForm form = new UpdateAppVersionForm();
        form.setId(99L);

        when(appVersionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appVersionController.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenNameChangedToExistingName_throwsBadRequestException() {
        UpdateAppVersionForm form = new UpdateAppVersionForm();
        form.setId(1L);
        form.setName("Taken");

        AppVersion appVersion = new AppVersion();
        appVersion.setId(1L);
        appVersion.setName("Old");

        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(appVersion));
        when(appVersionRepository.existsByName("Taken")).thenReturn(true);

        assertThatThrownBy(() -> appVersionController.update(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NAME_EXISTED);
    }

    @Test
    void update_whenRemovingLatestFlagFromCurrentLatest_throwsBadRequestException() {
        UpdateAppVersionForm form = new UpdateAppVersionForm();
        form.setId(1L);
        form.setName("Same");
        form.setIsLatest(false);

        AppVersion appVersion = new AppVersion();
        appVersion.setId(1L);
        appVersion.setName("Same");
        appVersion.setIsLatest(true);

        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(appVersion));

        assertThatThrownBy(() -> appVersionController.update(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NOT_HAVE_LATEST_VERSION);
    }

    @Test
    void update_whenFilePathChanged_deletesOldFileAndSaves() {
        UpdateAppVersionForm form = new UpdateAppVersionForm();
        form.setId(1L);
        form.setName("Same");
        form.setFilePath("new/path.apk");
        form.setIsLatest(false);

        AppVersion appVersion = new AppVersion();
        appVersion.setId(1L);
        appVersion.setName("Same");
        appVersion.setFilePath("old/path.apk");
        appVersion.setIsLatest(false);

        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(appVersion));
        when(redisService.buildKey("app-version", "latest")).thenReturn("app-version::latest");

        ApiMessageDto<Void> response = appVersionController.update(form);

        assertThat(response.getResult()).isTrue();
        verify(mediaService, times(1)).deleteFile("old/path.apk");
        verify(appVersionRepository, never()).resetLatest();
        verify(appVersionMapper, times(1)).fromUpdateAppVersionFormToEntity(form, appVersion);
        verify(appVersionRepository, times(1)).save(appVersion);
    }

    @Test
    void update_whenBecomingLatest_resetsPreviousLatest() {
        UpdateAppVersionForm form = new UpdateAppVersionForm();
        form.setId(1L);
        form.setName("Same");
        form.setFilePath("path.apk");
        form.setIsLatest(true);

        AppVersion appVersion = new AppVersion();
        appVersion.setId(1L);
        appVersion.setName("Same");
        appVersion.setFilePath("path.apk");
        appVersion.setIsLatest(false);

        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(appVersion));
        when(redisService.buildKey("app-version", "latest")).thenReturn("app-version::latest");

        ApiMessageDto<Void> response = appVersionController.update(form);

        assertThat(response.getResult()).isTrue();
        verify(appVersionRepository, times(1)).resetLatest();
        verify(mediaService, never()).deleteFile(anyString());
    }

    // ---------- delete ----------

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(appVersionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appVersionController.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenIsLatest_throwsBadRequestException() {
        AppVersion appVersion = new AppVersion();
        appVersion.setId(1L);
        appVersion.setIsLatest(true);

        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(appVersion));

        assertThatThrownBy(() -> appVersionController.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NOT_HAVE_LATEST_VERSION);
    }

    @Test
    void delete_whenNotLatest_deletesFileAndEntity() {
        AppVersion appVersion = new AppVersion();
        appVersion.setId(1L);
        appVersion.setIsLatest(false);
        appVersion.setFilePath("path.apk");

        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(appVersion));
        when(redisService.buildKey("app-version", "latest")).thenReturn("app-version::latest");

        ApiMessageDto<Void> response = appVersionController.delete(1L);

        assertThat(response.getResult()).isTrue();
        verify(mediaService, times(1)).deleteFile("path.apk");
        verify(appVersionRepository, times(1)).delete(appVersion);
    }

    // ---------- checkVersion ----------

    @Test
    void checkVersion_whenNameNotFound_throwsNotFoundException() {
        when(appVersionRepository.findFirstByName("1.0.0")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appVersionController.checkVersion("1.0.0"))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NOT_FOUND);
    }

    @Test
    void checkVersion_whenLatestNotFound_throwsNotFoundException() {
        AppVersion current = new AppVersion();
        current.setId(1L);
        when(appVersionRepository.findFirstByName("1.0.0")).thenReturn(Optional.of(current));
        when(appVersionRepository.findLatest()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appVersionController.checkVersion("1.0.0"))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.APP_VERSION_ERROR_NOT_FOUND);
    }

    @Test
    void checkVersion_whenCurrentIsLatest_updateNotRequired() {
        AppVersion current = new AppVersion();
        current.setId(1L);
        current.setForceUpdate(false);
        AppVersion latest = current;

        AppVersionDto latestDto = new AppVersionDto();

        when(appVersionRepository.findFirstByName("1.0.0")).thenReturn(Optional.of(current));
        when(appVersionRepository.findLatest()).thenReturn(Optional.of(latest));
        when(appVersionMapper.entityToAppVersionDto(latest)).thenReturn(latestDto);

        ApiMessageDto<CheckAppVersionDto> response = appVersionController.checkVersion("1.0.0");

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getUpdateRequired()).isFalse();
    }

    @Test
    void checkVersion_whenCurrentIsNotLatest_updateRequired() {
        AppVersion current = new AppVersion();
        current.setId(1L);

        AppVersion latest = new AppVersion();
        latest.setId(2L);
        latest.setForceUpdate(true);

        AppVersionDto latestDto = new AppVersionDto();

        when(appVersionRepository.findFirstByName("1.0.0")).thenReturn(Optional.of(current));
        when(appVersionRepository.findLatest()).thenReturn(Optional.of(latest));
        when(appVersionMapper.entityToAppVersionDto(latest)).thenReturn(latestDto);

        ApiMessageDto<CheckAppVersionDto> response = appVersionController.checkVersion("1.0.0");

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getUpdateRequired()).isTrue();
        assertThat(response.getData().getForceUpdate()).isTrue();
        assertThat(response.getData().getLatestVersion()).isEqualTo(latestDto);
    }
}
