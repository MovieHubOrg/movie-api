package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.setting.SettingDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.setting.CreateSettingForm;
import com.movie.api.form.setting.FindByGroupNameForm;
import com.movie.api.form.setting.FindByKeyNameForm;
import com.movie.api.form.setting.UpdateSettingForm;
import com.movie.api.mapper.SettingMapper;
import com.movie.api.service.SettingCacheService;
import com.movie.api.storage.criteria.SettingCriteria;
import com.movie.api.storage.model.Setting;
import com.movie.api.storage.repository.SettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/setting")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SettingController extends ABasicController {
    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private SettingMapper settingMapper;

    @Autowired
    private SettingCacheService settingCacheService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SET_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateSettingForm form) {
        if (settingRepository.findByKeyName(form.getKeyName()).isPresent()) {
            throw new BadRequestException(ErrorCode.SETTING_ERROR_EXISTED_GROUP_NAME_AND_KEY_NAME, "Key name existed");
        }

        Setting setting = settingMapper.fromCreateSettingFormToEntity(form);
        settingRepository.save(setting);
        settingCacheService.put(setting);
        return makeSuccessResponse("Create setting success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SET_V')")
    public ApiMessageDto<SettingDto> get(@PathVariable Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found setting", ErrorCode.SETTING_ERROR_NOT_FOUND));
        return makeSuccessResponse(settingMapper.fromEntityToSettingAdminDto(setting), "Get setting success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SET_L')")
    public ApiMessageDto<ResponseListDto<List<SettingDto>>> list(SettingCriteria criteria, Pageable pageable) {
        Page<Setting> settings = settingRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(
                makeResponseListDto(settings, settingMapper::fromEntityListToSettingAdminDtoList),
                "Get list setting success"
        );
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<SettingDto>>> autoComplete(SettingCriteria criteria) {
        Pageable pageable = PageRequest.of(0, 10);
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        Page<Setting> settings = settingRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(
                makeResponseListDto(settings, settingMapper::fromEntityListToSettingDtoAutoCompleteList),
                "Get list setting success"
        );
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SET_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateSettingForm form) {
        Setting setting = settingRepository.findById(form.getId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.SETTING_ERROR_NOT_FOUND, "Not found setting"));
        String oldKeyName = setting.getKeyName();

        boolean isKeyNameChanged = !form.getKeyName().equals(setting.getKeyName());
        if (isKeyNameChanged && settingRepository.findByKeyName(form.getKeyName()).isPresent()) {
            throw new BadRequestException(ErrorCode.SETTING_ERROR_EXISTED_GROUP_NAME_AND_KEY_NAME, "Key name existed");
        }

        settingMapper.fromUpdateSettingFormToEntity(form, setting);
        settingRepository.save(setting);

        if (!oldKeyName.equals(setting.getKeyName())) {
            settingCacheService.remove(oldKeyName);
        }
        settingCacheService.put(setting);
        return makeSuccessResponse("Update setting success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SET_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found setting", ErrorCode.SETTING_ERROR_NOT_FOUND));
        if (Boolean.TRUE.equals(setting.getIsSystem())) {
            throw new BadRequestException("[Setting] system setting cannot be deleted");
        }

        settingRepository.delete(setting);
        settingCacheService.remove(setting.getKeyName());
        return makeSuccessResponse("Delete setting success");
    }

    @GetMapping(value = "/find-by-key", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<List<SettingDto>> findByKey(FindByKeyNameForm form) {
        List<Setting> settings = settingRepository.findByKeyNames(form.getKeyNames(), false);
        return makeSuccessResponse(settingMapper.fromEntityListToSettingDtoList(settings), "Find key name success");
    }

    @GetMapping(value = "/find-by-group", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<List<SettingDto>> findByGroup(FindByGroupNameForm form) {
        List<Setting> settings = settingRepository.findByGroupNames(form.getGroupNames(), false);
        return makeSuccessResponse(settingMapper.fromEntityListToSettingDtoList(settings), "Find group name success");
    }

    @GetMapping(value = "/public", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<List<SettingDto>> listSetting() {
        List<Setting> settings = settingRepository.findAllByIsSystem(false);
        return makeSuccessResponse(settingMapper.fromEntityToSettingDtoPublicList(settings), "Get list setting success");
    }
}
