package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.moviePerson.MoviePersonDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.UpdateOrderingForm;
import com.movie.api.form.moviePerson.CreateMoviePersonForm;
import com.movie.api.form.moviePerson.UpdateMoviePersonForm;
import com.movie.api.mapper.MoviePersonMapper;
import com.movie.api.storage.criteria.MoviePersonCriteria;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.MoviePerson;
import com.movie.api.storage.model.Person;
import com.movie.api.storage.repository.MoviePersonRepository;
import com.movie.api.storage.repository.MovieRepository;
import com.movie.api.storage.repository.PersonRepository;
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
@RequestMapping("/v1/movie-person")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class MoviePersonController extends ABasicController {

    @Autowired
    private MoviePersonRepository moviePersonRepository;

    @Autowired
    private MoviePersonMapper moviePersonMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_P_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateMoviePersonForm form) {
        Movie movie = movieRepository.findById(form.getMovieId())
                .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));

        Person person = personRepository.findById(form.getPersonId())
                .orElseThrow(() -> new NotFoundException("[Person] not found", ErrorCode.PERSON_ERROR_NOT_FOUND));

        if (!person.getKinds().contains(form.getKind())) {
            throw new BadRequestException("[Person] not have kind", ErrorCode.PERSON_ERROR_NOT_HAVE_KIND);
        }

        MoviePerson moviePerson = moviePersonMapper.fromCreateMoviePersonFormToEntity(form);
        moviePerson.setMovie(movie);
        moviePerson.setPerson(person);
        moviePerson.setCharacterName(
                form.getCharacterName() != null && form.getKind().equals(BaseConstant.PERSON_KIND_ACTOR)
                        ? form.getCharacterName()
                        : null
        );

        int ordering = moviePersonRepository.findMaxOrdering(movie.getId(), form.getKind()).map(o -> o + 1).orElse(0);
        moviePerson.setOrdering(ordering);

        moviePersonRepository.save(moviePerson);
        return makeSuccessResponse("Create movie person successfully");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_P_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateMoviePersonForm form) {
        MoviePerson moviePerson = moviePersonRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Movie Person] Not found", ErrorCode.MOVIE_PERSON_ERROR_NOT_FOUND));

        if (form.getKind().equals(BaseConstant.PERSON_KIND_DIRECTOR) && form.getCharacterName() != null) {
            throw new BadRequestException("[Movie Person] Kind invalid", ErrorCode.MOVIE_PERSON_ERROR_KIND_INVALID);
        }

        moviePerson.setKind(form.getKind());
        moviePerson.setCharacterName(
                form.getCharacterName() != null && form.getKind().equals(BaseConstant.PERSON_KIND_ACTOR)
                        ? form.getCharacterName()
                        : null
        );

        moviePersonRepository.save(moviePerson);
        return makeSuccessResponse("Update movie persons success");
    }

    @PutMapping(value = "/update-ordering", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_P_U')")
    public ApiMessageDto<Void> updateOrdering(@RequestBody List<@Valid UpdateOrderingForm> form) {
        if (form == null || form.isEmpty()) {
            throw new BadRequestException("Input list cannot be empty", ErrorCode.MOVIE_PERSON_ERROR_INVALID_REQUEST);
        }

        List<Long> ids = form.stream()
                .map(UpdateOrderingForm::getId)
                .collect(Collectors.toList());
        List<MoviePerson> moviePersonList = moviePersonRepository.findAllById(ids);

        if (moviePersonList.size() != ids.size()) {
            throw new NotFoundException("[Movie Person] Not found", ErrorCode.MOVIE_PERSON_ERROR_NOT_FOUND);
        }

        Map<Long, Integer> orderingMap = form.stream()
                .collect(Collectors.toMap(UpdateOrderingForm::getId, UpdateOrderingForm::getOrdering));

        for (MoviePerson item : moviePersonList) {
            item.setOrdering(orderingMap.get(item.getId()));
        }

        moviePersonRepository.saveAll(moviePersonList);
        return makeSuccessResponse("Update movie person success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<MoviePersonDto>>> list(MoviePersonCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.ASC, "ordering")));
        Page<MoviePerson> moviePersonPage = moviePersonRepository.findAll(criteria.getSpecification(), pageable);

        return makeSuccessResponse(makeResponseListDto(moviePersonPage, moviePersonMapper::fromEntityToMoviePersonDtoList), "List movie person success");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_P_L')")
    public ApiMessageDto<ResponseListDto<List<MoviePersonDto>>> listForAdmin(MoviePersonCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.ASC, "ordering")));
        Page<MoviePerson> moviePersonPage = moviePersonRepository.findAll(criteria.getSpecification(), pageable);

        return makeSuccessResponse(makeResponseListDto(moviePersonPage, moviePersonMapper::fromEntityToMoviePersonDtoList), "List movie person success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_P_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        MoviePerson moviePerson = moviePersonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Movie Person] Not found]", ErrorCode.MOVIE_PERSON_ERROR_NOT_FOUND));

        moviePersonRepository.delete(moviePerson);
        return makeSuccessResponse("Delete movie person success");
    }
}
