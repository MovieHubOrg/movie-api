package com.movie.api.mapper;

import com.movie.api.dto.setting.SettingDto;
import com.movie.api.form.setting.CreateSettingForm;
import com.movie.api.form.setting.UpdateSettingForm;
import com.movie.api.storage.model.Setting;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SettingMapper {
    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "keyName", target = "keyName")
    @Mapping(source = "valueData", target = "valueData")
    @Mapping(source = "dataType", target = "dataType")
    @Mapping(source = "options", target = "options")
    @Mapping(source = "isSystem", target = "isSystem")
    @BeanMapping(ignoreByDefault = true)
    Setting fromCreateSettingFormToEntity(CreateSettingForm createSettingForm);

    @Mapping(source = "valueData", target = "valueData")
    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "keyName", target = "keyName")
    @Mapping(source = "dataType", target = "dataType")
    @Mapping(source = "options", target = "options")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateSettingFormToEntity(UpdateSettingForm updateSettingForm, @MappingTarget Setting setting);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "keyName", target = "keyName")
    @Mapping(source = "valueData", target = "valueData")
    @Mapping(source = "dataType", target = "dataType")
    @Mapping(source = "options", target = "options")
    @Mapping(source = "isSystem", target = "isSystem")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToSettingAdminDto")
    SettingDto fromEntityToSettingAdminDto(Setting setting);

    @IterableMapping(elementTargetType = SettingDto.class, qualifiedByName = "fromEntityToSettingAdminDto")
    List<SettingDto> fromEntityListToSettingAdminDtoList(List<Setting> settings);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "keyName", target = "keyName")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToSettingDtoAutoComplete")
    SettingDto fromEntityToSettingDtoAutoComplete(Setting setting);

    @IterableMapping(elementTargetType = SettingDto.class, qualifiedByName = "fromEntityToSettingDtoAutoComplete")
    List<SettingDto> fromEntityListToSettingDtoAutoCompleteList(List<Setting> settings);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "keyName", target = "keyName")
    @Mapping(source = "valueData", target = "valueData")
    @Mapping(source = "dataType", target = "dataType")
    @Mapping(source = "options", target = "options")
    @Mapping(source = "isSystem", target = "isSystem")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToSettingDto")
    SettingDto fromEntityToSettingDto(Setting setting);

    @IterableMapping(elementTargetType = SettingDto.class, qualifiedByName = "fromEntityToSettingDto")
    List<SettingDto> fromEntityListToSettingDtoList(List<Setting> settings);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "keyName", target = "keyName")
    @Mapping(source = "valueData", target = "valueData")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToSettingDtoPublic")
    SettingDto fromEntityToSettingDtoPublic(Setting setting);

    @IterableMapping(elementTargetType = SettingDto.class, qualifiedByName = "fromEntityToSettingDtoPublic")
    List<SettingDto> fromEntityToSettingDtoPublicList(List<Setting> settings);
}

