package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
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
import com.movie.api.utils.StringUtils;
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
@RequestMapping("/v1/category")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CategoryController extends ABasicController {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private MovieRepository movieRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CA_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateCategoryForm form) {
        if (categoryRepository.existsByName(form.getName())) {
            throw new BadRequestException("[Category] name existed", ErrorCode.CATEGORY_ERROR_NAME_EXISTED);
        }
        Category category = categoryMapper.fromCreateCategoryFormToEntity(form);
        category.setSlug(StringUtils.slugify(form.getName()));
        categoryRepository.save(category);
        return makeSuccessResponse("Create category success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<CategoryDto> get(@PathVariable("id") Long id) {
        Category category = categoryRepository.findByIdAndStatus(id, BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Category] Not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
        return makeSuccessResponse(categoryMapper.entityToCategoryDto(category), "Get category success.");
    }

    @GetMapping(value = "/admin/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CA_V')")
    public ApiMessageDto<CategoryDto> getForAdmin(@PathVariable("id") Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Category] Not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
        return makeSuccessResponse(categoryMapper.entityToCategoryDto(category), "Get category success.");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<CategoryDto>>> list(CategoryCriteria criteria, Pageable pageable) {
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        Page<Category> categories = categoryRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(categories, categoryMapper::fromEntityToCategoryDtoList), "List category success");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CA_L')")
    public ApiMessageDto<ResponseListDto<List<CategoryDto>>> listForAdmin(CategoryCriteria criteria, Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(categories, categoryMapper::fromEntityToCategoryDtoList), "List category success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<CategoryDto>>> autoComplete(CategoryCriteria criteria, Pageable pageable) {
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        Page<Category> categories = categoryRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(categories, categoryMapper::fromEntityToCategoryAutoCompleteDtoList), "List category success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CA_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateCategoryForm form) {
        Category category = categoryRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Category] Not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));

        if (!Objects.equals(category.getName(), form.getName())) {
            if (categoryRepository.existsByName(form.getName())) {
                throw new BadRequestException("[Category] name existed", ErrorCode.CATEGORY_ERROR_NAME_EXISTED);
            }
            category.setSlug(StringUtils.slugify(form.getName()));
        }

        categoryMapper.fromUpdateCategoryFormToEntity(form, category);
        categoryRepository.save(category);

        return makeSuccessResponse("Update category success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CA_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Category] Not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));

        if (movieRepository.existsByCategories_Id(category.getId())) {
            throw new BadRequestException("[Category] Cannot delete, still linked to movies", ErrorCode.CATEGORY_ERROR_HAS_MOVIE);
        }

        categoryRepository.delete(category);
        return makeSuccessResponse("Delete category success");
    }
}
