package com.movie.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.collectionItem.CollectionItemDto;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.UpdateOrderingForm;
import com.movie.api.form.collectionItem.CreateCollectionItemForm;
import com.movie.api.form.movie.FilterMovieForm;
import com.movie.api.mapper.CollectionItemMapper;
import com.movie.api.storage.criteria.CollectionItemCriteria;
import com.movie.api.storage.model.Collection;
import com.movie.api.storage.model.CollectionItem;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.repository.CollectionItemRepository;
import com.movie.api.storage.repository.CollectionRepository;
import com.movie.api.storage.repository.MovieRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/collection-item")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CollectionItemController extends ABasicController {
    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CollectionItemRepository collectionItemRepository;

    @Autowired
    private CollectionItemMapper collectionItemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_I_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateCollectionItemForm form) {
        Collection collection = collectionRepository.findById(form.getCollectionId())
                .orElseThrow(() -> new NotFoundException("[Collection] Not found", ErrorCode.COLLECTION_ERROR_NOT_FOUND));

        Movie movie = movieRepository.findById(form.getMovieId())
                .orElseThrow(() -> new NotFoundException("[Movie] Not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));

        if (collectionItemRepository.existsByCollectionIdAndMovieId(collection.getId(), movie.getId())) {
            throw new BadRequestException("[Collection Item] Movie already exists in this collection", ErrorCode.COLLECTION_ITEM_ERROR_MOVIE_EXISTED);
        }

        FilterMovieForm filter;
        try {
            filter = objectMapper.readValue(collection.getFilter(), FilterMovieForm.class);
        } catch (Exception e) {
            log.error("Failed to parse filter JSON for collectionId {}: {}", collection.getId(), collection.getFilter(), e);
            filter = null;
        }

        if (filter != null && filter.getLimit() != null) {
            if (filter.getLimit() == collectionItemRepository.countByCollectionId(collection.getId())) {
                throw new BadRequestException("[Collection Item] maximum item in collection", ErrorCode.COLLECTION_ITEM_ERROR_MAX_ITEM);
            }
        }

        CollectionItem collectionItem = new CollectionItem();
        collectionItem.setMovie(movie);
        collectionItem.setCollection(collection);
        int ordering = collectionItemRepository.findMaxOrdering(collection.getId()).map(o -> o + 1).orElse(0);
        collectionItem.setOrdering(ordering);
        collectionItemRepository.save(collectionItem);
        return makeSuccessResponse("Create collection item success");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_I_L')")
    public ApiMessageDto<ResponseListDto<List<CollectionItemDto>>> adminList(@RequestParam("collectionId") Long collectionId, Pageable pageable) {
        CollectionItemCriteria criteria = new CollectionItemCriteria();
        criteria.setCollectionId(collectionId);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.ASC, "ordering")));

        Page<CollectionItem> collectionItems = collectionItemRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(collectionItems, collectionItemMapper::entityToCollectionItemDtoList), "List collection item success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<MovieDto>>> list(@RequestParam("collectionId") Long collectionId, Pageable pageable) {
        CollectionItemCriteria criteria = new CollectionItemCriteria();
        criteria.setCollectionId(collectionId);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.ASC, "ordering")));

        Page<CollectionItem> collectionItems = collectionItemRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(collectionItems, collectionItemMapper::collectionItemsToMovieDtos), "List collection success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_I_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        CollectionItem collectionItem = collectionItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Collection Item] Not found", ErrorCode.COLLECTION_ITEM_ERROR_NOT_FOUND));
        collectionItemRepository.delete(collectionItem);
        return makeSuccessResponse("Delete collection item success");
    }

    @PutMapping(value = "/update-ordering", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COL_I_U')")
    public ApiMessageDto<Void> updateOrdering(@RequestBody List<@Valid UpdateOrderingForm> form) {
        if (form == null || form.isEmpty()) {
            throw new BadRequestException("Input list cannot be empty", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
        }

        List<Long> ids = form.stream()
                .map(UpdateOrderingForm::getId)
                .collect(Collectors.toList());
        List<CollectionItem> collectionItems = collectionItemRepository.findAllById(ids);

        if (collectionItems.size() != ids.size()) {
            throw new NotFoundException("[Collection Item] Not found", ErrorCode.COLLECTION_ITEM_ERROR_NOT_FOUND);
        }

        Map<Long, Integer> orderingMap = form.stream()
                .collect(Collectors.toMap(UpdateOrderingForm::getId, UpdateOrderingForm::getOrdering));

        for (CollectionItem item : collectionItems) {
            item.setOrdering(orderingMap.get(item.getId()));
        }
        collectionItemRepository.saveAll(collectionItems);

        return makeSuccessResponse("Update ordering collectionItems success");
    }
}
