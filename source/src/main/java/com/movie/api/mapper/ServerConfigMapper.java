package com.movie.api.mapper;

import com.movie.api.dto.serverconfig.ServerConfigDto;
import com.movie.api.form.serverconfig.CreateServerConfigForm;
import com.movie.api.form.serverconfig.UpdateServerConfigForm;
import com.movie.api.storage.model.ServerConfig;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServerConfigMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "serverNumber", target = "serverNumber")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "hostname", target = "hostname")
    @Mapping(source = "ip", target = "ip")
    @Mapping(source = "port", target = "port")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToServerConfigDto")
    ServerConfigDto entityToServerConfigDto(ServerConfig serverConfig);

    @IterableMapping(elementTargetType = ServerConfigDto.class, qualifiedByName = "entityToServerConfigDto")
    List<ServerConfigDto> fromEntityToServerConfigDtoList(List<ServerConfig> serverConfigs);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "serverNumber", target = "serverNumber")
    @Mapping(source = "name", target = "name")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToServerConfigAutoCompleteDto")
    ServerConfigDto entityToServerConfigAutoCompleteDto(ServerConfig serverConfig);

    @IterableMapping(elementTargetType = ServerConfigDto.class, qualifiedByName = "entityToServerConfigAutoCompleteDto")
    @Named("fromEntityToServerConfigAutoCompleteDtoList")
    List<ServerConfigDto> fromEntityToServerConfigAutoCompleteDtoList(List<ServerConfig> serverConfigs);

    @Mapping(source = "serverNumber", target = "serverNumber")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "hostname", target = "hostname")
    @Mapping(source = "ip", target = "ip")
    @Mapping(source = "port", target = "port")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    ServerConfig fromCreateServerConfigFormToEntity(CreateServerConfigForm form);

    @Mapping(source = "serverNumber", target = "serverNumber")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "hostname", target = "hostname")
    @Mapping(source = "ip", target = "ip")
    @Mapping(source = "port", target = "port")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateServerConfigFormToEntity(UpdateServerConfigForm form, @MappingTarget ServerConfig serverConfig);
}
