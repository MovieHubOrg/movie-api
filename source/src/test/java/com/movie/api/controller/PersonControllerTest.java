package com.movie.api.controller;

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

import java.util.ArrayList;
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
class PersonControllerTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonMapper personMapper;

    @Mock
    private MoviePersonRepository moviePersonRepository;

    @Mock
    private FavouriteRepository favouriteRepository;

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private PersonController personController;

    // ---------- create ----------

    @Test
    void create_savesPersonAndReturnsSuccess() {
        CreatePersonForm form = new CreatePersonForm();
        form.setName("John Doe");

        Person mappedEntity = new Person();
        when(personMapper.fromCreatePersonFormToEntity(form)).thenReturn(mappedEntity);

        ApiMessageDto<Void> response = personController.create(form);

        assertThat(response.getResult()).isTrue();
        verify(personRepository, times(1)).save(mappedEntity);
    }

    // ---------- get ----------

    @Test
    void get_whenFound_returnsSuccessResponse() {
        Person person = new Person();
        person.setId(1L);
        PersonDto dto = new PersonDto();

        when(personRepository.findByIdAndStatus(eq(1L), any())).thenReturn(Optional.of(person));
        when(personMapper.entityToPersonDto(person)).thenReturn(dto);

        ApiMessageDto<PersonDto> response = personController.get(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(personRepository.findByIdAndStatus(eq(99L), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personController.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERSON_ERROR_NOT_FOUND);
    }

    // ---------- getForAdmin ----------

    @Test
    void getForAdmin_whenFound_returnsSuccessResponse() {
        Person person = new Person();
        person.setId(1L);
        PersonDto dto = new PersonDto();

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personMapper.entityToPersonDto(person)).thenReturn(dto);

        ApiMessageDto<PersonDto> response = personController.getForAdmin(1L);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData()).isEqualTo(dto);
    }

    @Test
    void getForAdmin_whenNotFound_throwsNotFoundException() {
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personController.getForAdmin(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERSON_ERROR_NOT_FOUND);
    }

    // ---------- list ----------

    @Test
    void list_returnsSuccessResponseWrappingList() {
        PersonCriteria criteria = new PersonCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Person person = new Person();
        person.setId(1L);
        Page<Person> page = new PageImpl<>(Collections.singletonList(person));
        List<PersonDto> dtoList = Collections.singletonList(new PersonDto());

        when(personRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(personMapper.fromEntityToPersonDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<PersonDto>>> response = personController.list(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(criteria.getStatus()).isEqualTo(com.movie.api.constant.BaseConstant.STATUS_ACTIVE);
    }

    // ---------- listForAdmin ----------

    @Test
    void listForAdmin_returnsSuccessResponseWrappingList_withoutForcingStatus() {
        PersonCriteria criteria = new PersonCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Person person = new Person();
        person.setId(1L);
        Page<Person> page = new PageImpl<>(Collections.singletonList(person));
        List<PersonDto> dtoList = Collections.singletonList(new PersonDto());

        when(personRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(personMapper.fromEntityToPersonDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<PersonDto>>> response = personController.listForAdmin(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(criteria.getStatus()).isNull();
    }

    // ---------- autoComplete ----------

    @Test
    void autoComplete_returnsSuccessResponseWrappingList() {
        PersonCriteria criteria = new PersonCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Person person = new Person();
        person.setId(1L);
        Page<Person> page = new PageImpl<>(Collections.singletonList(person));
        List<PersonDto> dtoList = Collections.singletonList(new PersonDto());

        when(personRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(personMapper.fromEntityToPersonAutoCompleteDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<PersonDto>>> response = personController.autoComplete(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(criteria.getStatus()).isEqualTo(com.movie.api.constant.BaseConstant.STATUS_ACTIVE);
    }

    // ---------- autoCompleteForAdmin ----------

    @Test
    void autoCompleteForAdmin_returnsSuccessResponseWrappingList_withoutForcingStatus() {
        PersonCriteria criteria = new PersonCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Person person = new Person();
        person.setId(1L);
        Page<Person> page = new PageImpl<>(Collections.singletonList(person));
        List<PersonDto> dtoList = Collections.singletonList(new PersonDto());

        when(personRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(personMapper.fromEntityToPersonAutoCompleteDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<PersonDto>>> response = personController.autoCompleteForAdmin(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(criteria.getStatus()).isNull();
    }

    // ---------- update ----------

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        UpdatePersonForm form = new UpdatePersonForm();
        form.setId(99L);

        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personController.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERSON_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenAvatarPathChanged_deletesOldFileAndSaves() {
        UpdatePersonForm form = new UpdatePersonForm();
        form.setId(1L);
        form.setAvatarPath("new-avatar.png");

        Person person = new Person();
        person.setId(1L);
        person.setAvatarPath("old-avatar.png");
        person.setKinds(new ArrayList<>(Collections.singletonList(1)));

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        ApiMessageDto<Void> response = personController.update(form);

        assertThat(response.getResult()).isTrue();
        verify(mediaService, times(1)).deleteFile("old-avatar.png");
        verify(personMapper, times(1)).fromUpdatePersonFormToEntity(form, person);
        verify(personRepository, times(1)).save(person);
    }

    @Test
    void update_whenAvatarPathUnchanged_doesNotDeleteFile() {
        UpdatePersonForm form = new UpdatePersonForm();
        form.setId(1L);
        form.setAvatarPath("same-avatar.png");

        Person person = new Person();
        person.setId(1L);
        person.setAvatarPath("same-avatar.png");
        person.setKinds(new ArrayList<>(Collections.singletonList(1)));

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        ApiMessageDto<Void> response = personController.update(form);

        assertThat(response.getResult()).isTrue();
        verify(mediaService, never()).deleteFile(any());
        verify(personRepository, times(1)).save(person);
    }

    // ---------- delete ----------

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personController.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERSON_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenLinkedToMoviePerson_throwsBadRequestException() {
        Person person = new Person();
        person.setId(1L);

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(moviePersonRepository.existsByPersonId(1L)).thenReturn(true);

        assertThatThrownBy(() -> personController.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERSON_ERROR_MOVIE_PERSON_EXISTED);
    }

    @Test
    void delete_whenNotLinked_deletesAndReturnsSuccess() {
        Person person = new Person();
        person.setId(1L);
        person.setAvatarPath("avatar.png");
        person.setKinds(new ArrayList<>(Collections.singletonList(1)));

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(moviePersonRepository.existsByPersonId(1L)).thenReturn(false);

        ApiMessageDto<Void> response = personController.delete(1L);

        assertThat(response.getResult()).isTrue();
        verify(mediaService, times(1)).deleteFile("avatar.png");
        verify(favouriteRepository, times(1)).deleteByPersonId(1L);
        verify(personRepository, times(1)).delete(person);
    }
}
