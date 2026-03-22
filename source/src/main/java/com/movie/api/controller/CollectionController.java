package com.movie.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.collection.CollectionDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.UpdateOrderingForm;
import com.movie.api.form.collection.CreateCollectionForm;
import com.movie.api.form.collection.UpdateCollectionForm;
import com.movie.api.mapper.CollectionMapper;
import com.movie.api.service.CollectionService;
import com.movie.api.storage.criteria.CollectionCriteria;
import com.movie.api.storage.model.Collection;
import com.movie.api.storage.model.Style;
import com.movie.api.storage.repository.CollectionItemRepository;
import com.movie.api.storage.repository.CollectionRepository;
import com.movie.api.storage.repository.StyleRepository;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/collection")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CollectionController extends ABasicController {
    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private CollectionItemRepository collectionItemRepository;

    @Autowired
    private StyleRepository styleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CollectionService collectionService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateCollectionForm form) throws JsonProcessingException {
        if (collectionRepository.existsByName(form.getName())) {
            throw new BadRequestException("[Collection] name existed", ErrorCode.COLLECTION_ERROR_NAME_EXISTED);
        }

        Collection collection = collectionMapper.fromCreateCollectionFormToEntity(form);
        if (Objects.equals(collection.getType(), BaseConstant.COLLECTION_TYPE_SECTION) && form.getStyleId() != null) {
            Style style = styleRepository.findById(form.getStyleId())
                    .orElseThrow(() -> new NotFoundException("[Style] not found", ErrorCode.STYLE_ERROR_NOT_FOUND));
            collection.setStyle(style);
        }
        collection.setColor(objectMapper.writeValueAsString(form.getColors()));
        int ordering = collectionRepository.findMaxOrdering(form.getType()).map(o -> o + 1).orElse(0);
        collection.setOrdering(ordering);
        collectionRepository.save(collection);

        if (Boolean.TRUE.equals(form.getFillData())) {
            collectionService.fillDataForCollection(collection);
        }

        return makeSuccessResponse("Create collection success");
    }

    @GetMapping(value = "/admin/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_V')")
    public ApiMessageDto<CollectionDto> adminGet(@PathVariable("id") Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Collection] Not found", ErrorCode.COLLECTION_ERROR_NOT_FOUND));
        return makeSuccessResponse(collectionMapper.entityToCollectionDto(collection), "Get collection success.");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_L')")
    public ApiMessageDto<ResponseListDto<List<CollectionDto>>> adminList(CollectionCriteria criteria, Pageable pageable) {
        Page<Collection> collections = collectionRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(collections, collectionMapper::entityToCollectionDtoList), "List collection success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<CollectionDto> get(@PathVariable("id") Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Collection] Not found", ErrorCode.COMMENT_ERROR_NOT_FOUND));
        return makeSuccessResponse(collectionMapper.entityToCollectionDto(collection), "Get collection success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<CollectionDto>>> list(CollectionCriteria criteria, Pageable pageable) {
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        criteria.setType(BaseConstant.COLLECTION_TYPE_SECTION);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.ASC, "ordering")));
        Page<Collection> collections = collectionRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(collections, collectionMapper::entityToCollectionDetailsDtoList), "List collection success");
    }

    @GetMapping(value = "/topics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<CollectionDto>>> topics(CollectionCriteria criteria, Pageable pageable) {
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        criteria.setType(BaseConstant.COLLECTION_TYPE_TOPIC);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.ASC, "ordering")));
        Page<Collection> collections = collectionRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(collections, collectionMapper::entityToCollectionDtoList), "List collection success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateCollectionForm form) throws JsonProcessingException {
        Collection collection = collectionRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Collection] Not found", ErrorCode.COLLECTION_ERROR_NOT_FOUND));

        if (!Objects.equals(form.getName(), collection.getName()) && collectionRepository.existsByName(form.getName())) {
            throw new BadRequestException("[Collection] name existed", ErrorCode.COLLECTION_ERROR_NAME_EXISTED);
        }

        if (Objects.equals(form.getType(), BaseConstant.COLLECTION_TYPE_SECTION) && form.getStyleId() != null) {
            Style style = styleRepository.findById(form.getStyleId())
                    .orElseThrow(() -> new NotFoundException("[Style] not found", ErrorCode.STYLE_ERROR_NOT_FOUND));
            collection.setStyle(style);
        }

        collectionMapper.fromUpdateCollectionFormToEntity(form, collection);
        collection.setColor(objectMapper.writeValueAsString(form.getColors()));
        collectionRepository.save(collection);
        return makeSuccessResponse("Update collection success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Collection] Not found", ErrorCode.COLLECTION_ERROR_NOT_FOUND));
        collectionItemRepository.deleteByCollectionId(collection.getId());
        collectionRepository.delete(collection);
        return makeSuccessResponse("Delete collection success");
    }

    @PutMapping(value = "/update-ordering", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_U')")
    public ApiMessageDto<Void> updateOrdering(@RequestBody List<@Valid UpdateOrderingForm> form) {
        if (form == null || form.isEmpty()) {
            throw new BadRequestException("Input list cannot be empty", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
        }

        List<Long> ids = form.stream()
                .map(UpdateOrderingForm::getId)
                .collect(Collectors.toList());
        List<Collection> collections = collectionRepository.findAllById(ids);

        if (collections.size() != ids.size()) {
            throw new NotFoundException("[Collection] Not found", ErrorCode.COLLECTION_ERROR_NOT_FOUND);
        }

        Map<Long, Integer> orderingMap = form.stream()
                .collect(Collectors.toMap(UpdateOrderingForm::getId, UpdateOrderingForm::getOrdering));

        for (Collection item : collections) {
            item.setOrdering(orderingMap.get(item.getId()));
        }
        collectionRepository.saveAll(collections);

        return makeSuccessResponse("Update ordering collections success");
    }
}
