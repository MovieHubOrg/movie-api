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
import com.movie.api.mapper.CollectionItemMapper;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.model.Collection;
import com.movie.api.storage.model.CollectionItem;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.repository.CollectionItemRepository;
import com.movie.api.storage.repository.CollectionRepository;
import com.movie.api.storage.repository.MovieRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionItemControllerTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CollectionItemRepository collectionItemRepository;

    @Mock
    private CollectionItemMapper collectionItemMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private CollectionItemController collectionItemController;

    // ---------- create ----------

    @Test
    void create_whenCollectionNotFound_throwsNotFoundException() {
        CreateCollectionItemForm form = new CreateCollectionItemForm();
        form.setCollectionId(1L);
        form.setMovieId(2L);

        when(collectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionItemController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenMovieNotFound_throwsNotFoundException() {
        CreateCollectionItemForm form = new CreateCollectionItemForm();
        form.setCollectionId(1L);
        form.setMovieId(2L);

        Collection collection = new Collection();
        collection.setId(1L);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(movieRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionItemController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenMovieAlreadyInCollection_throwsBadRequestException() {
        CreateCollectionItemForm form = new CreateCollectionItemForm();
        form.setCollectionId(1L);
        form.setMovieId(2L);

        Collection collection = new Collection();
        collection.setId(1L);
        Movie movie = new Movie();
        movie.setId(2L);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie));
        when(collectionItemRepository.existsByCollectionIdAndMovieId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> collectionItemController.create(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ITEM_ERROR_MOVIE_EXISTED);
    }

    @Test
    void create_whenFilterParseFailsAndNoLimit_savesSuccessfully() throws Exception {
        CreateCollectionItemForm form = new CreateCollectionItemForm();
        form.setCollectionId(1L);
        form.setMovieId(2L);

        Collection collection = new Collection();
        collection.setId(1L);
        collection.setFilter("not-json");
        Movie movie = new Movie();
        movie.setId(2L);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie));
        when(collectionItemRepository.existsByCollectionIdAndMovieId(1L, 2L)).thenReturn(false);
        when(objectMapper.readValue(eq("not-json"), eq(com.movie.api.form.movie.FilterMovieForm.class)))
                .thenThrow(new RuntimeException("bad json"));
        when(collectionItemRepository.findMaxOrdering(1L)).thenReturn(Optional.empty());

        ApiMessageDto<Void> response = collectionItemController.create(form);

        assertThat(response.getResult()).isTrue();
        verify(collectionItemRepository, times(1)).save(any(CollectionItem.class));
        verify(redisService, times(1)).deleteByPrefix(any());
    }

    @Test
    void create_whenMaxItemReached_throwsBadRequestException() throws Exception {
        CreateCollectionItemForm form = new CreateCollectionItemForm();
        form.setCollectionId(1L);
        form.setMovieId(2L);

        Collection collection = new Collection();
        collection.setId(1L);
        collection.setFilter("{\"limit\":2}");
        Movie movie = new Movie();
        movie.setId(2L);

        com.movie.api.form.movie.FilterMovieForm filter = new com.movie.api.form.movie.FilterMovieForm();
        filter.setLimit(2);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie));
        when(collectionItemRepository.existsByCollectionIdAndMovieId(1L, 2L)).thenReturn(false);
        when(objectMapper.readValue(eq("{\"limit\":2}"), eq(com.movie.api.form.movie.FilterMovieForm.class)))
                .thenReturn(filter);
        when(collectionItemRepository.countByCollectionId(1L)).thenReturn(2);

        assertThatThrownBy(() -> collectionItemController.create(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ITEM_ERROR_MAX_ITEM);
    }

    @Test
    void create_whenValidAndNoOrderingYet_savesWithOrderingZero() throws Exception {
        CreateCollectionItemForm form = new CreateCollectionItemForm();
        form.setCollectionId(1L);
        form.setMovieId(2L);

        Collection collection = new Collection();
        collection.setId(1L);
        collection.setFilter(null);
        Movie movie = new Movie();
        movie.setId(2L);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie));
        when(collectionItemRepository.existsByCollectionIdAndMovieId(1L, 2L)).thenReturn(false);
        when(objectMapper.readValue((String) null, com.movie.api.form.movie.FilterMovieForm.class))
                .thenThrow(new RuntimeException("null filter"));
        when(collectionItemRepository.findMaxOrdering(1L)).thenReturn(Optional.empty());

        ApiMessageDto<Void> response = collectionItemController.create(form);

        assertThat(response.getResult()).isTrue();
        verify(collectionItemRepository, times(1)).save(any(CollectionItem.class));
    }

    // ---------- adminList ----------

    @Test
    void adminList_returnsSuccessResponseWrappingList() {
        Pageable pageable = PageRequest.of(0, 10);
        CollectionItem item = new CollectionItem();
        item.setId(1L);
        Page<CollectionItem> page = new PageImpl<>(Collections.singletonList(item));
        List<CollectionItemDto> dtoList = Collections.singletonList(new CollectionItemDto());

        when(collectionItemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(collectionItemMapper.entityToCollectionItemDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<CollectionItemDto>>> response =
                collectionItemController.adminList(1L, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
    }

    // ---------- list ----------

    @Test
    void list_whenCached_returnsCachedResponseWithoutHittingDb() {
        Pageable pageable = PageRequest.of(0, 10);
        ResponseListDto<List<MovieDto>> cached = new ResponseListDto<>(Collections.singletonList(new MovieDto()), 1L, 1);

        when(redisService.buildKey(any(), any(), any(), any(), any())).thenReturn("key");
        when(redisService.get(eq("key"), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(cached);

        ApiMessageDto<ResponseListDto<List<MovieDto>>> response = collectionItemController.list(1L, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(cached);
        verify(collectionItemRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void list_whenNotCachedAndHasContent_putsResultInCache() {
        Pageable pageable = PageRequest.of(0, 10);
        CollectionItem item = new CollectionItem();
        item.setId(1L);
        Page<CollectionItem> page = new PageImpl<>(Collections.singletonList(item));
        List<MovieDto> movieDtoList = Collections.singletonList(new MovieDto());

        when(redisService.buildKey(any(), any(), any(), any(), any())).thenReturn("key");
        when(redisService.get(eq("key"), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(null);
        when(collectionItemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(collectionItemMapper.collectionItemsToMovieDtos(page.getContent())).thenReturn(movieDtoList);

        ApiMessageDto<ResponseListDto<List<MovieDto>>> response = collectionItemController.list(1L, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(movieDtoList);
        verify(redisService, times(1)).put(eq("key"), any(), eq(5 * 60));
    }

    @Test
    void list_whenNotCachedAndEmpty_doesNotPutInCache() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CollectionItem> page = new PageImpl<>(Collections.emptyList());

        when(redisService.buildKey(any(), any(), any(), any(), any())).thenReturn("key");
        when(redisService.get(eq("key"), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(null);
        when(collectionItemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(collectionItemMapper.collectionItemsToMovieDtos(page.getContent())).thenReturn(Collections.emptyList());

        ApiMessageDto<ResponseListDto<List<MovieDto>>> response = collectionItemController.list(1L, pageable);

        assertThat(response.getResult()).isTrue();
        verify(redisService, never()).put(any(), any(), any(Integer.class));
    }

    // ---------- delete ----------

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(collectionItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionItemController.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ITEM_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenFound_deletesAndClearsCache() {
        Collection collection = new Collection();
        collection.setId(1L);
        CollectionItem item = new CollectionItem();
        item.setId(2L);
        item.setCollection(collection);

        when(collectionItemRepository.findById(2L)).thenReturn(Optional.of(item));

        ApiMessageDto<Void> response = collectionItemController.delete(2L);

        assertThat(response.getResult()).isTrue();
        verify(collectionItemRepository, times(1)).delete(item);
        verify(redisService, times(1)).deleteByPrefix(any());
    }

    // ---------- updateOrdering ----------

    @Test
    void updateOrdering_whenFormEmpty_throwsBadRequestException() {
        assertThatThrownBy(() -> collectionItemController.updateOrdering(Collections.emptyList()))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
    }

    @Test
    void updateOrdering_whenFormNull_throwsBadRequestException() {
        assertThatThrownBy(() -> collectionItemController.updateOrdering(null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
    }

    @Test
    void updateOrdering_whenSomeIdsNotFound_throwsNotFoundException() {
        UpdateOrderingForm form1 = new UpdateOrderingForm();
        form1.setId(1L);
        form1.setOrdering(1);
        UpdateOrderingForm form2 = new UpdateOrderingForm();
        form2.setId(2L);
        form2.setOrdering(2);

        CollectionItem item1 = new CollectionItem();
        item1.setId(1L);

        when(collectionItemRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Collections.singletonList(item1));

        assertThatThrownBy(() -> collectionItemController.updateOrdering(Arrays.asList(form1, form2)))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ITEM_ERROR_NOT_FOUND);
    }

    @Test
    void updateOrdering_whenAllFound_updatesOrderingAndClearsCache() {
        UpdateOrderingForm form1 = new UpdateOrderingForm();
        form1.setId(1L);
        form1.setOrdering(5);

        Collection collection = new Collection();
        collection.setId(10L);
        CollectionItem item1 = new CollectionItem();
        item1.setId(1L);
        item1.setCollection(collection);

        when(collectionItemRepository.findAllById(Collections.singletonList(1L))).thenReturn(Collections.singletonList(item1));

        ApiMessageDto<Void> response = collectionItemController.updateOrdering(Collections.singletonList(form1));

        assertThat(response.getResult()).isTrue();
        assertThat(item1.getOrdering()).isEqualTo(5);
        verify(collectionItemRepository, times(1)).saveAll(Collections.singletonList(item1));
        verify(redisService, times(1)).deleteByPrefix(any());
    }
}
