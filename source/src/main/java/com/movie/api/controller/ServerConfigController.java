package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.serverconfig.ServerConfigDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.serverconfig.CreateServerConfigForm;
import com.movie.api.form.serverconfig.UpdateServerConfigForm;
import com.movie.api.mapper.ServerConfigMapper;
import com.movie.api.storage.criteria.ServerConfigCriteria;
import com.movie.api.storage.model.ServerConfig;
import com.movie.api.storage.repository.ServerConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/server-config")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ServerConfigController extends ABasicController {
    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @Autowired
    private ServerConfigMapper serverConfigMapper;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SC_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateServerConfigForm form) {
        if (serverConfigRepository.existsByServerNumber(form.getServerNumber())) {
            throw new BadRequestException("[ServerConfig] serverNumber existed", ErrorCode.SERVER_CONFIG_ERROR_SERVER_NUMBER_EXISTED);
        }
        if (serverConfigRepository.existsByHostname(form.getHostname())) {
            throw new BadRequestException("[ServerConfig] hostname existed", ErrorCode.SERVER_CONFIG_ERROR_HOSTNAME_EXISTED);
        }
        if (serverConfigRepository.existsByIpAndPort(form.getIp(), form.getPort())) {
            throw new BadRequestException("[ServerConfig] ip and port combination existed", ErrorCode.SERVER_CONFIG_ERROR_IP_PORT_EXISTED);
        }

        ServerConfig serverConfig = serverConfigMapper.fromCreateServerConfigFormToEntity(form);
        serverConfigRepository.save(serverConfig);
        return makeSuccessResponse("Create server config success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SC_V')")
    public ApiMessageDto<ServerConfigDto> get(@PathVariable Long id) {
        ServerConfig serverConfig = serverConfigRepository.findByIdAndStatus(id, BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[ServerConfig] Not found", ErrorCode.SERVER_CONFIG_ERROR_NOT_FOUND));
        return makeSuccessResponse(serverConfigMapper.entityToServerConfigDto(serverConfig), "Get server config success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SC_L')")
    public ApiMessageDto<ResponseListDto<List<ServerConfigDto>>> list(ServerConfigCriteria criteria, Pageable pageable) {
        Page<ServerConfig> serverConfigs = serverConfigRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(serverConfigs, serverConfigMapper::fromEntityToServerConfigDtoList), "List server config success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SC_L')")
    public ApiMessageDto<ResponseListDto<List<ServerConfigDto>>> autoComplete(ServerConfigCriteria criteria, Pageable pageable) {
        Page<ServerConfig> serverConfigs = serverConfigRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(serverConfigs, serverConfigMapper::fromEntityToServerConfigAutoCompleteDtoList), "List server config success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SC_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateServerConfigForm form) {
        ServerConfig serverConfig = serverConfigRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[ServerConfig] Not found", ErrorCode.SERVER_CONFIG_ERROR_NOT_FOUND));

        if (!Objects.equals(serverConfig.getServerNumber(), form.getServerNumber())) {
            if (serverConfigRepository.existsByServerNumberAndIdNot(form.getServerNumber(), form.getId())) {
                throw new BadRequestException("[ServerConfig] serverNumber existed", ErrorCode.SERVER_CONFIG_ERROR_SERVER_NUMBER_EXISTED);
            }
        }

        if (!Objects.equals(serverConfig.getHostname(), form.getHostname())) {
            if (serverConfigRepository.existsByHostnameAndIdNot(form.getHostname(), form.getId())) {
                throw new BadRequestException("[ServerConfig] hostname existed", ErrorCode.SERVER_CONFIG_ERROR_HOSTNAME_EXISTED);
            }
        }

        if (!Objects.equals(serverConfig.getIp(), form.getIp()) || !Objects.equals(serverConfig.getPort(), form.getPort())) {
            if (serverConfigRepository.existsByIpAndPortAndIdNot(form.getIp(), form.getPort(), form.getId())) {
                throw new BadRequestException("[ServerConfig] ip and port combination existed", ErrorCode.SERVER_CONFIG_ERROR_IP_PORT_EXISTED);
            }
        }

        serverConfigMapper.fromUpdateServerConfigFormToEntity(form, serverConfig);
        serverConfigRepository.save(serverConfig);

        return makeSuccessResponse("Update server config success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SC_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        ServerConfig serverConfig = serverConfigRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[ServerConfig] Not found", ErrorCode.SERVER_CONFIG_ERROR_NOT_FOUND));

        serverConfigRepository.delete(serverConfig);
        return makeSuccessResponse("Delete server config success");
    }
}
