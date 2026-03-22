package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.person.PersonDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.person.CreatePersonForm;
import com.movie.api.form.person.UpdatePersonForm;
import com.movie.api.mapper.PersonMapper;
import com.movie.api.service.MediaService;
import com.movie.api.storage.criteria.PersonCriteria;
import com.movie.api.storage.model.Person;
import com.movie.api.storage.repository.FavouriteRepository;
import com.movie.api.storage.repository.MoviePersonRepository;
import com.movie.api.storage.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/person")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class PersonController extends ABasicController {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonMapper personMapper;

    @Autowired
    private MoviePersonRepository moviePersonRepository;

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private MediaService mediaService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PSN_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreatePersonForm form) {
        Person person = personMapper.fromCreatePersonFormToEntity(form);
        personRepository.save(person);
        return makeSuccessResponse("Create person success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<PersonDto> get(@PathVariable("id") Long id) {
        Person person = personRepository.findByIdAndStatus(id, BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Person] Not found", ErrorCode.PERSON_ERROR_NOT_FOUND));

        return makeSuccessResponse(personMapper.entityToPersonDto(person), "Get person success");
    }

    @GetMapping(value = "/admin/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PSN_V')")
    public ApiMessageDto<PersonDto> getForAdmin(@PathVariable("id") Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Person] Not found", ErrorCode.PERSON_ERROR_NOT_FOUND));

        return makeSuccessResponse(personMapper.entityToPersonDto(person), "Get person success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<PersonDto>>> list(PersonCriteria criteria, Pageable pageable) {
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "id")
        );
        Page<Person> movies = personRepository.findAll(criteria.getSpecification(), pageable);

        return makeSuccessResponse(makeResponseListDto(movies, personMapper::fromEntityToPersonDtoList), "List person success");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PSN_L')")
    public ApiMessageDto<ResponseListDto<List<PersonDto>>> listForAdmin(PersonCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "id")
        );
        Page<Person> movies = personRepository.findAll(criteria.getSpecification(), pageable);

        return makeSuccessResponse(makeResponseListDto(movies, personMapper::fromEntityToPersonDtoList), "List person success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<PersonDto>>> autoComplete(PersonCriteria criteria, Pageable pageable) {
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "id")
        );
        Page<Person> movies = personRepository.findAll(criteria.getSpecification(), pageable);

        return makeSuccessResponse(makeResponseListDto(movies, personMapper::fromEntityToPersonAutoCompleteDtoList), "List auto complete person success");
    }

    @GetMapping(value = "/admin/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PSN_L')")
    public ApiMessageDto<ResponseListDto<List<PersonDto>>> autoCompleteForAdmin(PersonCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "id")
        );Page<Person> movies = personRepository.findAll(criteria.getSpecification(), pageable);
        ResponseListDto<List<PersonDto>> responseListDto = makeResponseListDto(movies, personMapper::fromEntityToPersonAutoCompleteDtoList);
        return makeSuccessResponse(responseListDto, "List auto complete person success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PSN_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdatePersonForm form) {
        Person person = personRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Person] Not found", ErrorCode.PERSON_ERROR_NOT_FOUND));

        if (!Objects.equals(form.getAvatarPath(), person.getAvatarPath())) {
            mediaService.deleteFile(person.getAvatarPath());
        }

        person.getKinds().clear();
        personMapper.fromUpdatePersonFormToEntity(form, person);

        personRepository.save(person);
        return makeSuccessResponse("Update person success");
    }

    @Transactional("tenantTransactionManager")
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PSN_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Person] Not found", ErrorCode.PERSON_ERROR_NOT_FOUND));

        if (moviePersonRepository.existsByPersonId(person.getId())) {
            throw new BadRequestException("[Person] Cannot delete with relationship with Movie Person", ErrorCode.PERSON_ERROR_MOVIE_PERSON_EXISTED);
        }

        mediaService.deleteFile(person.getAvatarPath());

        favouriteRepository.deleteByPersonId(person.getId());
        person.getKinds().clear();
        personRepository.delete(person);
        return makeSuccessResponse("Delete person success");
    }
}
