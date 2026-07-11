package com.movie.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.criteria.CollectionCriteria;
import com.movie.api.storage.model.Collection;
import com.movie.api.storage.model.Style;
import com.movie.api.storage.repository.CollectionItemRepository;
import com.movie.api.storage.repository.CollectionRepository;
import com.movie.api.storage.repository.StyleRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionControllerTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private CollectionItemRepository collectionItemRepository;

    @Mock
    private StyleRepository styleRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CollectionService collectionService;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private CollectionController collectionController;

    // ---------- create ----------

    @Test
    void create_whenNameExists_throwsBadRequestException() throws Exception {
        CreateCollectionForm form = new CreateCollectionForm();
        form.setName("Trending");

        when(collectionRepository.existsByName("Trending")).thenReturn(true);

        assertThatThrownBy(() -> collectionController.create(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ERROR_NAME_EXISTED);
    }

    @Test
    void create_whenSectionTypeAndStyleNotFound_throwsNotFoundException() throws Exception {
        CreateCollectionForm form = new CreateCollectionForm();
        form.setName("Section A");
        form.setStyleId(5L);

        Collection mapped = new Collection();
        mapped.setType(BaseConstant.COLLECTION_TYPE_SECTION);

        when(collectionRepository.existsByName("Section A")).thenReturn(false);
        when(collectionMapper.fromCreateCollectionFormToEntity(form)).thenReturn(mapped);
        when(styleRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.STYLE_ERROR_NOT_FOUND);
    }

    @Test
    void create_whenSectionTypeAndStyleFound_savesAndClearsCache() throws Exception {
        CreateCollectionForm form = new CreateCollectionForm();
        form.setName("Section B");
        form.setStyleId(5L);
        form.setColors(Arrays.asList("#FFFFFF", "#000000"));
        form.setFillData(false);

        Collection mapped = new Collection();
        mapped.setType(BaseConstant.COLLECTION_TYPE_SECTION);
        Style style = new Style();
        style.setId(5L);

        when(collectionRepository.existsByName("Section B")).thenReturn(false);
        when(collectionMapper.fromCreateCollectionFormToEntity(form)).thenReturn(mapped);
        when(styleRepository.findById(5L)).thenReturn(Optional.of(style));
        when(objectMapper.writeValueAsString(form.getColors())).thenReturn("[\"#FFFFFF\",\"#000000\"]");
        when(collectionRepository.findMaxOrdering(BaseConstant.COLLECTION_TYPE_SECTION)).thenReturn(Optional.of(3));
        when(redisService.buildKey("collection", "list", "section")).thenReturn("collection::list::section");

        ApiMessageDto<Void> response = collectionController.create(form);

        assertThat(response.getResult()).isTrue();
        assertThat(mapped.getStyle()).isEqualTo(style);
        assertThat(mapped.getOrdering()).isEqualTo(4);
        verify(collectionRepository, times(1)).save(mapped);
        verify(collectionService, never()).fillDataForCollection(any());
        verify(redisService, times(1)).deleteByPrefix("collection::list::section");
    }

    @Test
    void create_whenTopicType_doesNotClearSectionCache() throws Exception {
        CreateCollectionForm form = new CreateCollectionForm();
        form.setName("Topic A");
        form.setColors(Arrays.asList("#FFFFFF", "#000000"));
        form.setFillData(false);

        Collection mapped = new Collection();
        mapped.setType(BaseConstant.COLLECTION_TYPE_TOPIC);

        when(collectionRepository.existsByName("Topic A")).thenReturn(false);
        when(collectionMapper.fromCreateCollectionFormToEntity(form)).thenReturn(mapped);
        when(objectMapper.writeValueAsString(form.getColors())).thenReturn("[\"#FFFFFF\",\"#000000\"]");
        when(collectionRepository.findMaxOrdering(BaseConstant.COLLECTION_TYPE_TOPIC)).thenReturn(Optional.empty());

        ApiMessageDto<Void> response = collectionController.create(form);

        assertThat(response.getResult()).isTrue();
        assertThat(mapped.getOrdering()).isEqualTo(0);
        verify(redisService, never()).deleteByPrefix(anyString());
    }

    @Test
    void create_whenFillDataTrue_callsFillDataForCollection() throws Exception {
        CreateCollectionForm form = new CreateCollectionForm();
        form.setName("Topic B");
        form.setColors(Arrays.asList("#FFFFFF", "#000000"));
        form.setFillData(true);

        Collection mapped = new Collection();
        mapped.setType(BaseConstant.COLLECTION_TYPE_TOPIC);

        when(collectionRepository.existsByName("Topic B")).thenReturn(false);
        when(collectionMapper.fromCreateCollectionFormToEntity(form)).thenReturn(mapped);
        when(objectMapper.writeValueAsString(form.getColors())).thenReturn("[\"#FFFFFF\",\"#000000\"]");
        when(collectionRepository.findMaxOrdering(BaseConstant.COLLECTION_TYPE_TOPIC)).thenReturn(Optional.empty());

        ApiMessageDto<Void> response = collectionController.create(form);

        assertThat(response.getResult()).isTrue();
        verify(collectionService, times(1)).fillDataForCollection(mapped);
    }

    // ---------- adminGet ----------

    @Test
    void adminGet_whenFound_returnsSuccessResponse() {
        Collection collection = new Collection();
        collection.setId(1L);
        CollectionDto dto = new CollectionDto();

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(collectionMapper.entityToCollectionDto(collection)).thenReturn(dto);

        ApiMessageDto<CollectionDto> response = collectionController.adminGet(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void adminGet_whenNotFound_throwsNotFoundException() {
        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionController.adminGet(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ERROR_NOT_FOUND);
    }

    // ---------- adminList ----------

    @Test
    void adminList_returnsSuccessResponseWrappingList() {
        CollectionCriteria criteria = new CollectionCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Collection collection = new Collection();
        collection.setId(1L);
        Page<Collection> page = new PageImpl<>(Collections.singletonList(collection));
        List<CollectionDto> dtoList = Collections.singletonList(new CollectionDto());

        when(collectionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(collectionMapper.entityToCollectionDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<CollectionDto>>> response = collectionController.adminList(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
    }

    // ---------- get ----------

    @Test
    void get_whenFound_returnsSuccessResponse() {
        Collection collection = new Collection();
        collection.setId(1L);
        CollectionDto dto = new CollectionDto();

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(collectionMapper.entityToCollectionDto(collection)).thenReturn(dto);

        ApiMessageDto<CollectionDto> response = collectionController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionController.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ERROR_NOT_FOUND);
    }

    // ---------- list ----------

    @Test
    void list_whenCached_returnsCachedValueWithoutQueryingDb() {
        Pageable pageable = PageRequest.of(0, 10);
        when(redisService.buildKey(eq("collection"), eq("list"), eq("section"), anyString(), anyString()))
                .thenReturn("collection::list::section::0::10");
        ResponseListDto<List<CollectionDto>> cached = new ResponseListDto<>(Collections.emptyList(), 0L, 0);
        when(redisService.get(eq("collection::list::section::0::10"), any(TypeReference.class))).thenReturn(cached);

        ApiMessageDto<ResponseListDto<List<CollectionDto>>> response = collectionController.list(pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(cached);
        verify(collectionRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void list_whenNotCached_fetchesFromDbAndCaches() {
        Pageable pageable = PageRequest.of(0, 10);
        when(redisService.buildKey(eq("collection"), eq("list"), eq("section"), anyString(), anyString()))
                .thenReturn("collection::list::section::0::10");
        when(redisService.get(eq("collection::list::section::0::10"), any(TypeReference.class))).thenReturn(null);

        Collection collection = new Collection();
        collection.setId(1L);
        Page<Collection> page = new PageImpl<>(Collections.singletonList(collection));
        List<CollectionDto> dtoList = Collections.singletonList(new CollectionDto());

        when(collectionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(collectionMapper.entityToCollectionDetailsDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<CollectionDto>>> response = collectionController.list(pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        verify(redisService, times(1)).put(eq("collection::list::section::0::10"), any(), eq(5 * 60));
    }

    // ---------- topics ----------

    @Test
    void topics_returnsSuccessResponseWrappingList() {
        CollectionCriteria criteria = new CollectionCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Collection collection = new Collection();
        collection.setId(1L);
        Page<Collection> page = new PageImpl<>(Collections.singletonList(collection));
        List<CollectionDto> dtoList = Collections.singletonList(new CollectionDto());

        when(collectionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(collectionMapper.entityToCollectionDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<CollectionDto>>> response = collectionController.topics(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(criteria.getType()).isEqualTo(BaseConstant.COLLECTION_TYPE_TOPIC);
        assertThat(criteria.getStatus()).isEqualTo(BaseConstant.STATUS_ACTIVE);
    }

    // ---------- update ----------

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        UpdateCollectionForm form = new UpdateCollectionForm();
        form.setId(99L);

        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionController.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenNameChangedToExistingName_throwsBadRequestException() {
        UpdateCollectionForm form = new UpdateCollectionForm();
        form.setId(1L);
        form.setName("Taken");

        Collection collection = new Collection();
        collection.setId(1L);
        collection.setName("Old");

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(collectionRepository.existsByName("Taken")).thenReturn(true);

        assertThatThrownBy(() -> collectionController.update(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ERROR_NAME_EXISTED);
    }

    @Test
    void update_whenSectionTypeAndStyleNotFound_throwsNotFoundException() {
        UpdateCollectionForm form = new UpdateCollectionForm();
        form.setId(1L);
        form.setName("Same");
        form.setType(BaseConstant.COLLECTION_TYPE_SECTION);
        form.setStyleId(5L);

        Collection collection = new Collection();
        collection.setId(1L);
        collection.setName("Same");

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(styleRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionController.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.STYLE_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenTypeChangesToSection_clearsSectionCache() throws Exception {
        UpdateCollectionForm form = new UpdateCollectionForm();
        form.setId(1L);
        form.setName("Same");
        form.setType(BaseConstant.COLLECTION_TYPE_SECTION);
        form.setColors(Arrays.asList("#FFFFFF", "#000000"));

        Collection collection = new Collection();
        collection.setId(1L);
        collection.setName("Same");
        collection.setType(BaseConstant.COLLECTION_TYPE_TOPIC);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(objectMapper.writeValueAsString(form.getColors())).thenReturn("[\"#FFFFFF\",\"#000000\"]");
        when(redisService.buildKey("collection", "list", "section")).thenReturn("collection::list::section");

        ApiMessageDto<Void> response = collectionController.update(form);

        assertThat(response.getResult()).isTrue();
        verify(collectionMapper, times(1)).fromUpdateCollectionFormToEntity(form, collection);
        verify(collectionRepository, times(1)).save(collection);
        verify(redisService, times(1)).deleteByPrefix("collection::list::section");
    }

    @Test
    void update_whenTypeStaysTopic_doesNotClearSectionCache() throws Exception {
        UpdateCollectionForm form = new UpdateCollectionForm();
        form.setId(1L);
        form.setName("Same");
        form.setType(BaseConstant.COLLECTION_TYPE_TOPIC);
        form.setColors(Arrays.asList("#FFFFFF", "#000000"));

        Collection collection = new Collection();
        collection.setId(1L);
        collection.setName("Same");
        collection.setType(BaseConstant.COLLECTION_TYPE_TOPIC);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(objectMapper.writeValueAsString(form.getColors())).thenReturn("[\"#FFFFFF\",\"#000000\"]");

        ApiMessageDto<Void> response = collectionController.update(form);

        assertThat(response.getResult()).isTrue();
        verify(redisService, never()).deleteByPrefix(anyString());
    }

    // ---------- delete ----------

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(collectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionController.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenSectionType_deletesItemsAndClearsCache() {
        Collection collection = new Collection();
        collection.setId(1L);
        collection.setType(BaseConstant.COLLECTION_TYPE_SECTION);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(redisService.buildKey("collection", "list", "section")).thenReturn("collection::list::section");

        ApiMessageDto<Void> response = collectionController.delete(1L);

        assertThat(response.getResult()).isTrue();
        verify(collectionItemRepository, times(1)).deleteByCollectionId(1L);
        verify(collectionRepository, times(1)).delete(collection);
        verify(redisService, times(1)).deleteByPrefix("collection::list::section");
    }

    @Test
    void delete_whenTopicType_doesNotClearSectionCache() {
        Collection collection = new Collection();
        collection.setId(1L);
        collection.setType(BaseConstant.COLLECTION_TYPE_TOPIC);

        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));

        ApiMessageDto<Void> response = collectionController.delete(1L);

        assertThat(response.getResult()).isTrue();
        verify(redisService, never()).deleteByPrefix(anyString());
    }

    // ---------- updateOrdering ----------

    @Test
    void updateOrdering_whenListEmpty_throwsBadRequestException() {
        assertThatThrownBy(() -> collectionController.updateOrdering(Collections.emptyList()))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
    }

    @Test
    void updateOrdering_whenListNull_throwsBadRequestException() {
        assertThatThrownBy(() -> collectionController.updateOrdering(null))
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

        Collection collection1 = new Collection();
        collection1.setId(1L);

        when(collectionRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Collections.singletonList(collection1));

        assertThatThrownBy(() -> collectionController.updateOrdering(Arrays.asList(form1, form2)))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.COLLECTION_ERROR_NOT_FOUND);
    }

    @Test
    void updateOrdering_whenAllFound_updatesOrderingAndClearsCacheIfSectionPresent() {
        UpdateOrderingForm form1 = new UpdateOrderingForm();
        form1.setId(1L);
        form1.setOrdering(10);
        UpdateOrderingForm form2 = new UpdateOrderingForm();
        form2.setId(2L);
        form2.setOrdering(20);

        Collection collection1 = new Collection();
        collection1.setId(1L);
        collection1.setType(BaseConstant.COLLECTION_TYPE_SECTION);
        Collection collection2 = new Collection();
        collection2.setId(2L);
        collection2.setType(BaseConstant.COLLECTION_TYPE_TOPIC);

        when(collectionRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(collection1, collection2));
        when(redisService.buildKey("collection", "list", "section")).thenReturn("collection::list::section");

        ApiMessageDto<Void> response = collectionController.updateOrdering(Arrays.asList(form1, form2));

        assertThat(response.getResult()).isTrue();
        assertThat(collection1.getOrdering()).isEqualTo(10);
        assertThat(collection2.getOrdering()).isEqualTo(20);
        verify(collectionRepository, times(1)).saveAll(Arrays.asList(collection1, collection2));
        verify(redisService, times(1)).deleteByPrefix("collection::list::section");
    }

    @Test
    void updateOrdering_whenNoSectionPresent_doesNotClearCache() {
        UpdateOrderingForm form1 = new UpdateOrderingForm();
        form1.setId(1L);
        form1.setOrdering(10);

        Collection collection1 = new Collection();
        collection1.setId(1L);
        collection1.setType(BaseConstant.COLLECTION_TYPE_TOPIC);

        when(collectionRepository.findAllById(Collections.singletonList(1L))).thenReturn(Collections.singletonList(collection1));

        ApiMessageDto<Void> response = collectionController.updateOrdering(Collections.singletonList(form1));

        assertThat(response.getResult()).isTrue();
        verify(redisService, never()).deleteByPrefix(anyString());
    }
}
