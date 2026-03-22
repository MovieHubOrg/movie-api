package com.movie.api.controller;

import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.style.StyleDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.style.CreateStyleForm;
import com.movie.api.form.style.UpdateStyleForm;
import com.movie.api.mapper.StyleMapper;
import com.movie.api.service.MediaService;
import com.movie.api.storage.criteria.StyleCriteria;
import com.movie.api.storage.model.Style;
import com.movie.api.storage.repository.CollectionRepository;
import com.movie.api.storage.repository.StyleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/style")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class StyleController extends ABasicController {
    @Autowired
    private StyleRepository styleRepository;

    @Autowired
    private StyleMapper styleMapper;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private MediaService mediaService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STL_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateStyleForm form) {
        if (styleRepository.existsByType(form.getType())) {
            throw new BadRequestException("[Style] type existed", ErrorCode.STYLE_ERROR_TYPE_EXISTED);
        }

        if (form.getIsDefault()) {
            styleRepository.resetDefault();
        } else if (!styleRepository.existsByIdNotNull()) {
            form.setIsDefault(true);
        }

        Style style = styleMapper.fromCreateStyleFormToEntity(form);
        styleRepository.save(style);
        return makeSuccessResponse("Create style success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STL_V')")
    public ApiMessageDto<StyleDto> get(@PathVariable("id") Long id) {
        Style style = styleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Style] Not found", ErrorCode.STYLE_ERROR_NOT_FOUND));
        return makeSuccessResponse(styleMapper.entityToStyleDto(style), "Get style success.");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STL_L')")
    public ApiMessageDto<ResponseListDto<List<StyleDto>>> list(StyleCriteria criteria, Pageable pageable) {
        Page<Style> styles = styleRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(styles, styleMapper::entityToStyleDtoList), "List style success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<StyleDto>>> autoComplete(StyleCriteria criteria, Pageable pageable) {
        Page<Style> styles = styleRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(styles, styleMapper::entityToStyleAutoCompleteDtoList), "List style success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STL_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateStyleForm form) {
        Style style = styleRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Style] Not found", ErrorCode.STYLE_ERROR_NOT_FOUND));

        if (style.getIsDefault() && !form.getIsDefault()) {
            throw new BadRequestException("[Style] not have default", ErrorCode.STYLE_ERROR_TYPE_NOT_HAVE_DEFAULT);
        }

        List<String> deletedFiles = new ArrayList<>();
        if (!Objects.equals(form.getImageMobileUrl(), style.getImageMobileUrl())) {
            deletedFiles.add(style.getImageMobileUrl());
        }

        if (!Objects.equals(form.getImageWebUrl(), style.getImageWebUrl())) {
            deletedFiles.add(style.getImageWebUrl());
        }
        mediaService.deleteFiles(deletedFiles);

        if (!style.getIsDefault() && form.getIsDefault()) {
            styleRepository.resetDefault();
        }

        styleMapper.fromUpdateStyleFormToEntity(form, style);
        styleRepository.save(style);
        return makeSuccessResponse("Update style success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STL_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Style style = styleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Style] Not found", ErrorCode.STYLE_ERROR_NOT_FOUND));
        if (style.getIsDefault()) {
            throw new BadRequestException("[Style] not have default", ErrorCode.STYLE_ERROR_TYPE_NOT_HAVE_DEFAULT);
        }

        List<String> deletedFiles = new ArrayList<>();
        deletedFiles.add(style.getImageMobileUrl());
        deletedFiles.add(style.getImageWebUrl());
        mediaService.deleteFiles(deletedFiles);

        collectionRepository.updateStyleToDefault(style.getId());
        styleRepository.delete(style);
        return makeSuccessResponse("Delete style success.");
    }
}
