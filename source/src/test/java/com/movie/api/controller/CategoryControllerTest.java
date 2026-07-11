package com.movie.api.controller;

import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.category.CategoryDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.category.CreateCategoryForm;
import com.movie.api.form.category.UpdateCategoryForm;
import com.movie.api.mapper.CategoryMapper;
import com.movie.api.storage.criteria.CategoryCriteria;
import com.movie.api.storage.model.Category;
import com.movie.api.storage.repository.CategoryRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private CategoryController categoryController;

    // ---------- create ----------

    @Test
    void create_whenNameExists_throwsBadRequestException() {
        CreateCategoryForm form = new CreateCategoryForm();
        form.setName("Action");
        form.setStatus(1);

        when(categoryRepository.existsByName("Action")).thenReturn(true);

        assertThatThrownBy(() -> categoryController.create(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_ERROR_NAME_EXISTED);
    }

    @Test
    void create_whenNameIsNew_savesCategoryWithSlugAndReturnsSuccess() {
        CreateCategoryForm form = new CreateCategoryForm();
        form.setName("Sci Fi Movies");
        form.setStatus(1);

        Category mappedEntity = new Category();
        when(categoryRepository.existsByName("Sci Fi Movies")).thenReturn(false);
        when(categoryMapper.fromCreateCategoryFormToEntity(form)).thenReturn(mappedEntity);

        ApiMessageDto<Void> response = categoryController.create(form);

        assertThat(response.getResult()).isTrue();
        assertThat(mappedEntity.getSlug()).isNotBlank();
        verify(categoryRepository, times(1)).save(mappedEntity);
    }

    // ---------- get ----------

    @Test
    void get_whenFound_returnsSuccessResponse() {
        Category category = new Category();
        category.setId(1L);
        CategoryDto dto = new CategoryDto();

        when(categoryRepository.findByIdAndStatus(eq(1L), any())).thenReturn(Optional.of(category));
        when(categoryMapper.entityToCategoryDto(category)).thenReturn(dto);

        ApiMessageDto<CategoryDto> response = categoryController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(categoryRepository.findByIdAndStatus(eq(99L), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryController.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_ERROR_NOT_FOUND);
    }

    // ---------- getForAdmin ----------

    @Test
    void getForAdmin_whenFound_returnsSuccessResponse() {
        Category category = new Category();
        category.setId(1L);
        CategoryDto dto = new CategoryDto();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.entityToCategoryDto(category)).thenReturn(dto);

        ApiMessageDto<CategoryDto> response = categoryController.getForAdmin(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void getForAdmin_whenNotFound_throwsNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryController.getForAdmin(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_ERROR_NOT_FOUND);
    }

    // ---------- list ----------

    @Test
    void list_returnsSuccessResponseWrappingList() {
        CategoryCriteria criteria = new CategoryCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category();
        category.setId(1L);
        Page<Category> page = new PageImpl<>(Collections.singletonList(category));
        List<CategoryDto> dtoList = Collections.singletonList(new CategoryDto());

        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(categoryMapper.fromEntityToCategoryDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<CategoryDto>>> response = categoryController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(response.getData().getTotalElements()).isEqualTo(1);
        assertThat(criteria.getStatus()).isEqualTo(com.movie.api.constant.BaseConstant.STATUS_ACTIVE);
    }

    // ---------- listForAdmin ----------

    @Test
    void listForAdmin_returnsSuccessResponseWrappingList_withoutForcingStatus() {
        CategoryCriteria criteria = new CategoryCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category();
        category.setId(1L);
        Page<Category> page = new PageImpl<>(Collections.singletonList(category));
        List<CategoryDto> dtoList = Collections.singletonList(new CategoryDto());

        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(categoryMapper.fromEntityToCategoryDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<CategoryDto>>> response = categoryController.listForAdmin(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(criteria.getStatus()).isNull();
    }

    // ---------- autoComplete ----------

    @Test
    void autoComplete_returnsSuccessResponseWrappingAutoCompleteList() {
        CategoryCriteria criteria = new CategoryCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category();
        category.setId(1L);
        Page<Category> page = new PageImpl<>(Collections.singletonList(category));
        List<CategoryDto> dtoList = Collections.singletonList(new CategoryDto());

        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(categoryMapper.fromEntityToCategoryAutoCompleteDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<CategoryDto>>> response = categoryController.autoComplete(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(criteria.getStatus()).isEqualTo(com.movie.api.constant.BaseConstant.STATUS_ACTIVE);
    }

    // ---------- update ----------

    @Test
    void update_whenIdNotFound_throwsNotFoundException() {
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(99L);
        form.setName("New Name");
        form.setStatus(1);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryController.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenNameChangedToExistingName_throwsBadRequestException() {
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(1L);
        form.setName("Taken Name");
        form.setStatus(1);

        Category category = new Category();
        category.setId(1L);
        category.setName("Old Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Taken Name")).thenReturn(true);

        assertThatThrownBy(() -> categoryController.update(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_ERROR_NAME_EXISTED);
    }

    @Test
    void update_whenNameChangedToNewUniqueName_regeneratesSlugAndSaves() {
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(1L);
        form.setName("Brand New Name");
        form.setStatus(1);

        Category category = new Category();
        category.setId(1L);
        category.setName("Old Name");
        category.setSlug("old-name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Brand New Name")).thenReturn(false);

        ApiMessageDto<Void> response = categoryController.update(form);

        assertThat(response.getResult()).isTrue();
        assertThat(category.getSlug()).isNotEqualTo("old-name");
        verify(categoryMapper, times(1)).fromUpdateCategoryFormToEntity(form, category);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void update_whenNameUnchanged_doesNotRegenerateSlugButStillSaves() {
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(1L);
        form.setName("Same Name");
        form.setStatus(1);

        Category category = new Category();
        category.setId(1L);
        category.setName("Same Name");
        category.setSlug("same-name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        ApiMessageDto<Void> response = categoryController.update(form);

        assertThat(response.getResult()).isTrue();
        assertThat(category.getSlug()).isEqualTo("same-name");
        verify(categoryRepository, never()).existsByName(any());
        verify(categoryMapper, times(1)).fromUpdateCategoryFormToEntity(form, category);
        verify(categoryRepository, times(1)).save(category);
    }

    // ---------- delete ----------

    @Test
    void delete_whenIdNotFound_throwsNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryController.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenLinkedToMovies_throwsBadRequestException() {
        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(movieRepository.existsByCategories_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryController.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_ERROR_HAS_MOVIE);
    }

    @Test
    void delete_whenNotLinkedToMovies_deletesAndReturnsSuccess() {
        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(movieRepository.existsByCategories_Id(1L)).thenReturn(false);

        ApiMessageDto<Void> response = categoryController.delete(1L);

        assertThat(response.getResult()).isTrue();
        verify(categoryRepository, times(1)).delete(category);
    }
}
