package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.favourite.FavouriteDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.favourite.CreateFavouriteForm;
import com.movie.api.mapper.FavouriteMapper;
import com.movie.api.storage.criteria.FavouriteCriteria;
import com.movie.api.storage.model.*;
import com.movie.api.storage.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/favourite")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class FavouriteController extends ABasicController {

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private FavouriteMapper favouriteMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Long> create(@Valid @RequestBody CreateFavouriteForm form) {
        Account user = accountRepository.findByIdAndStatusAndKind(getCurrentUser(), BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER)
                .orElseThrow(() -> new NotFoundException("[User] user not found"));

        Favourite favourite = favouriteRepository.findByUserIdAndTypeAndTargetId(user.getId(), form.getType(), form.getTargetId())
                .orElse(null);
        if (favourite != null) {
            return makeSuccessResponse(favourite.getId(), "Create favourite success");
        }

        favourite = new Favourite();
        favourite.setUser(user);
        favourite.setType(form.getType());

        if (Objects.equals(form.getType(), BaseConstant.FAVOURITE_TYPE_MOVIE)) {
            Movie movie = movieRepository.findById(form.getTargetId())
                    .orElseThrow(() -> new NotFoundException("[Movie] not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));
            favourite.setMovie(movie);
        } else {
            Person person = personRepository.findById(form.getTargetId())
                    .orElseThrow(() -> new NotFoundException("[Person] not found", ErrorCode.PERSON_ERROR_NOT_FOUND));
            favourite.setPerson(person);
        }

        favourite = favouriteRepository.save(favourite);
        return makeSuccessResponse(favourite.getId(), "Create favourite success");
    }

    @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<FavouriteDto> get(@RequestParam("targetId") Long targetId, @RequestParam("type") Integer type) {
        Favourite favourite = favouriteRepository.findByUserIdAndTypeAndTargetId(getCurrentUser(), type, targetId).orElse(null);
        if (favourite == null) {
            return makeErrorResponse("Favourite not found");
        }
        FavouriteDto favouriteDto = new FavouriteDto();
        favouriteDto.setId(favourite.getId());
        return makeSuccessResponse(favouriteDto, "Get favourite success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<FavouriteDto>>> list(FavouriteCriteria criteria, Pageable pageable) {
        criteria.setUserId(getCurrentUser());
        Page<Favourite> favourites = favouriteRepository.findAll(criteria.getSpecification(), pageable);

        return makeSuccessResponse(makeResponseListDto(favourites, favouriteMapper::fromEntityToFavouriteDtoList), "List favourite success");
    }

    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> delete(@RequestParam("targetId") Long targetId, @RequestParam("type") Integer type) {
        Favourite favourite = favouriteRepository.findByUserIdAndTypeAndTargetId(getCurrentUser(), type, targetId)
                .orElseThrow(() -> new NotFoundException("[Favourite] Not found", ErrorCode.FAVOURITE_ERROR_NOT_FOUND));
        favouriteRepository.delete(favourite);
        return makeSuccessResponse("Delete favourite success");
    }

    @GetMapping(value = "/get-list-ids", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<List<Long>> getListId(@RequestParam("type") Integer type, @RequestParam(value = "movieId", required = false) Long movieId) {
        List<Long> ids;
        Long userId = getCurrentUser();
        if (Objects.equals(type, BaseConstant.FAVOURITE_TYPE_MOVIE)) {
            ids = favouriteRepository.findFavouriteMovieIds(userId);
        } else if (Objects.equals(type, BaseConstant.FAVOURITE_TYPE_PERSON)) {
            ids = favouriteRepository.findFavouritePersonIds(userId, movieId);
        } else {
            throw new BadRequestException("Invalid type");
        }
        return makeSuccessResponse(ids, "List favourite success");
    }
}
