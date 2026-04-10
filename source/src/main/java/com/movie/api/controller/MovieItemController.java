package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.movieItem.MovieItemDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.UpdateOrderingForm;
import com.movie.api.form.movieItem.CreateMovieItemForm;
import com.movie.api.form.movieItem.UpdateMovieItemForm;
import com.movie.api.mapper.MovieItemMapper;
import com.movie.api.service.MediaService;
import com.movie.api.service.MovieService;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.criteria.MovieItemCriteria;
import com.movie.api.storage.model.Movie;
import com.movie.api.storage.model.MovieItem;
import com.movie.api.storage.model.VideoLibrary;
import com.movie.api.storage.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/movie-item")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class MovieItemController extends ABasicController {
    @Autowired
    private MovieItemRepository movieItemRepository;

    @Autowired
    private MovieItemMapper movieItemMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private VideoLibraryRepository videoLibraryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

    @Autowired
    private MovieService movieService;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_I_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateMovieItemForm form) {
        Movie movie = movieRepository.findById(form.getMovieId())
                .orElseThrow(() -> new NotFoundException("[Movie] Movie not found", ErrorCode.MOVIE_ERROR_NOT_FOUND));
        MovieItem parent = null;
        VideoLibrary video = null;

        if (Objects.equals(form.getKind(), BaseConstant.MOVIE_ITEM_KIND_EPISODE) && Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SINGLE)) {
            throw new BadRequestException("[Movie Item] cannot create episode for movie type season", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
        }

        if (!Objects.equals(form.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON) && form.getParentId() == null) {
            throw new BadRequestException("[Movie Item] Parent is required", ErrorCode.MOVIE_ITEM_ERROR_PARENT_REQUIRED);
        }

        checkLabel(form.getKind(), form.getLabel(), form.getMovieId(), form.getParentId());

        if (form.getVideoId() != null) {
            video = videoLibraryRepository.findById(form.getVideoId())
                    .orElseThrow(() -> new NotFoundException("[Video Library] Video not found", ErrorCode.VIDEO_LIBRARY_ERROR_NOT_FOUND));
        }

        // if episode or trailer -> required parent
        if (!Objects.equals(form.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON)) {
            parent = movieItemRepository.findById(form.getParentId())
                    .orElseThrow(() -> new NotFoundException("[Movie Item] Parent not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND));

            // cannot create trailer for episode
            if (form.getKind().equals(BaseConstant.MOVIE_ITEM_KIND_TRAILER) && Objects.equals(parent.getKind(), BaseConstant.MOVIE_ITEM_KIND_EPISODE)) {
                throw new BadRequestException("[Movie Item] cannot create trailer for episode", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
            }

            // cannot create episode for episode
            if (form.getKind().equals(BaseConstant.MOVIE_ITEM_KIND_EPISODE) && Objects.equals(parent.getKind(), BaseConstant.MOVIE_ITEM_KIND_EPISODE)) {
                throw new BadRequestException("[Movie Item] cannot create episode for episode", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
            }

            if (form.getKind().equals(BaseConstant.MOVIE_ITEM_KIND_EPISODE)
                    && parent.getTotalEpisode() != null
                    && parent.getTotalEpisode() <= movieItemRepository.countCurrentTotalEpisodes(parent.getId())) {
                throw new BadRequestException("[Movie Item] Invalid total episode", ErrorCode.MOVIE_ITEM_ERROR_INVALID_TOTAL_EPISODES);
            }
            form.setTotalEpisode(null);
        }

        MovieItem movieItem = movieItemMapper.fromCreateMovieItemFormToEntity(form);
        movieItem.setParent(parent);
        movieItem.setVideo(video);
        movieItem.setMovie(movie);
        int ordering = movieItemRepository.findMaxOrdering(movie.getId(), form.getKind(), form.getParentId())
                .map(o -> o + 1).orElse(0);
        movieItem.setOrdering(ordering);
        movieItem = movieItemRepository.save(movieItem);

        handleUpdateLatestMovieItem(movieItem, form.getIsLatest());

        redisService.delete(redisService.buildKey("movie", movie.getId().toString()));

        return makeSuccessResponse("Create movie item success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<MovieItemDto> get(@PathVariable("id") Long id) {
        MovieItem movieItem = movieItemRepository.findByIdAndStatus(id, BaseConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Movie Item] Not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND));

        return makeSuccessResponse(movieItemMapper.entityToMovieItemDto(movieItem), "Get movie item success");
    }

    @GetMapping(value = "/admin/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_I_V')")
    public ApiMessageDto<MovieItemDto> getForAdmin(@PathVariable("id") Long id) {
        MovieItem movieItem = movieItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Movie Item] Not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND));

        return makeSuccessResponse(movieItemMapper.entityToMovieItemDto(movieItem), "Get movie item success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<MovieItemDto>>> list(MovieItemCriteria criteria, Pageable pageable) {
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.ASC, "ordering")));
        Page<MovieItem> movieItems = movieItemRepository.findAll(criteria.getSpecification(), pageable);

        return makeSuccessResponse(makeResponseListDto(movieItems, movieItemMapper::fromEntityToMovieItemAutoCompleteDtoList), "List movie item success");
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_I_L')")
    public ApiMessageDto<ResponseListDto<List<MovieItemDto>>> listForAdmin(MovieItemCriteria criteria, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(new Sort.Order(Sort.Direction.ASC, "ordering")));
        Page<MovieItem> movieItems = movieItemRepository.findAll(criteria.getSpecification(), pageable);

        return makeSuccessResponse(makeResponseListDto(movieItems, movieItemMapper::fromEntityToMovieItemAutoCompleteDtoList), "List movie item success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_I_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateMovieItemForm form) {
        MovieItem movieItem = movieItemRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Movie Item] Not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND));

        boolean isRequiredVideo = false;
        VideoLibrary video = null;

        if (Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON)) {
            if (Objects.equals(movieItem.getMovie().getType(), BaseConstant.MOVIE_TYPE_SINGLE)) {
                isRequiredVideo = true;
            }
            if (form.getTotalEpisode() != null && form.getTotalEpisode() < movieItemRepository.countCurrentTotalEpisodes(movieItem.getId())) {
                throw new BadRequestException("[Movie Item] Invalid total episode", ErrorCode.MOVIE_ITEM_ERROR_INVALID_TOTAL_EPISODES);
            }
        } else {
            isRequiredVideo = true;
            form.setTotalEpisode(null);
        }

        if (isRequiredVideo && form.getVideoId() != null) {
            video = videoLibraryRepository.findById(form.getVideoId())
                    .orElseThrow(() -> new NotFoundException("[Video Library] Video not found", ErrorCode.VIDEO_LIBRARY_ERROR_NOT_FOUND));
        }

        if (!Objects.equals(form.getLabel(), movieItem.getLabel())) {
            Long parentId = movieItem.getParent() != null ? movieItem.getParent().getId() : null;
            checkLabel(movieItem.getKind(), form.getLabel(), movieItem.getMovie().getId(), parentId);
        }

        if (StringUtils.isNoneBlank(form.getThumbnailUrl())
                && StringUtils.isNoneBlank(movieItem.getThumbnailUrl())
                && !Objects.equals(form.getThumbnailUrl(), movieItem.getThumbnailUrl())) {
            mediaService.deleteFile(movieItem.getThumbnailUrl());
        }
        movieItemMapper.fromUpdateMovieItemFormToEntity(form, movieItem);
        movieItem.setVideo(video);
        movieItemRepository.save(movieItem);
        redisService.delete(redisService.buildKey("movie", movieItem.getMovie().getId().toString()));
        return makeSuccessResponse("Update movie item success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_I_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        MovieItem movieItem = movieItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Movie Item] Not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND));

        if (movieItem.getThumbnailUrl() != null) {
            mediaService.deleteFile(movieItem.getThumbnailUrl());
        }

        List<Long> movieItemIds = new ArrayList<>();
        movieItemIds.add(id);
        if (Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON)) {
            movieItemIds.addAll(movieItemRepository.findIdByParentIdAndKindNot(id, BaseConstant.MOVIE_ITEM_KIND_TRAILER));
        }
        watchHistoryRepository.deleteByMovieItemIds(movieItemIds);

        commentRepository.deleteByMovieItemId(id);

        movieItemRepository.delete(movieItem);

        if (Boolean.TRUE.equals(movieItem.getIsLatest())) {
            movieItemRepository.findFirstByMovieIdAndKindAndIdNotOrderByOrderingDesc(
                    movieItem.getMovie().getId(),
                    movieItem.getKind(),
                    movieItem.getId()
            ).ifPresentOrElse(
                    nextLatest -> {
                        nextLatest.setIsLatest(true);
                        movieItemRepository.save(nextLatest);
                        movieService.updateMetaDataMovie(nextLatest);
                    },
                    () -> movieService.resetMetaDataMovie(movieItem.getMovie())
            );
        }

        redisService.delete(redisService.buildKey("movie", movieItem.getMovie().getId().toString()));

        return makeSuccessResponse("Delete movie item success");
    }

    @PutMapping(value = "/update-ordering", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_I_U')")
    public ApiMessageDto<Void> updateOrdering(@RequestBody List<@Valid UpdateOrderingForm> form) {
        if (form == null || form.isEmpty()) {
            throw new BadRequestException("Input list cannot be empty", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
        }

        List<Long> ids = form.stream()
                .map(UpdateOrderingForm::getId)
                .collect(Collectors.toList());

        Map<Long, MovieItem> itemMap = movieItemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(MovieItem::getId, Function.identity()));
        if (itemMap.size() != form.size()) {
            throw new NotFoundException("[Movie Item] Not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND);
        }
        for (UpdateOrderingForm f : form) {
            MovieItem movieItem = itemMap.get(f.getId());
            movieItem.setOrdering(f.getOrdering());
        }

        movieItemRepository.saveAll(itemMap.values());
        return makeSuccessResponse("Update movie item success");
    }

    @PutMapping(value = "/mark-latest/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MOV_I_U')")
    public ApiMessageDto<Void> markLatest(@PathVariable Long id) {
        MovieItem movieItem = movieItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Movie Item] Not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND));
        if (Boolean.TRUE.equals(movieItem.getIsLatest())) {
            return makeSuccessResponse("Movie item is already marked as latest");
        }
        Movie movie = movieItem.getMovie();
        if (Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SERIES) && Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON)) {
            throw new BadRequestException("Cannot mark episode latest for series movie", ErrorCode.MOVIE_ITEM_ERROR_INVALID_REQUEST);
        }
        handleUpdateLatestMovieItem(movieItem, true);
        redisService.delete(redisService.buildKey("movie", movieItem.getMovie().getId().toString()));
        return makeSuccessResponse("Mark latest movie item success");
    }

    // check label existed by kind not trailer
    private void checkLabel(Integer kind, String label, Long movieId, Long parentId) {
        if (Objects.equals(kind, BaseConstant.MOVIE_ITEM_KIND_TRAILER)) {
            return;
        }

        boolean labelExists;
        if (Objects.equals(kind, BaseConstant.MOVIE_ITEM_KIND_SEASON)) {
            labelExists = movieItemRepository.existsByMovieIdAndKindAndLabelAndParentIsNull(movieId, kind, label);
        } else {
            labelExists = movieItemRepository.existsByMovieIdAndKindAndLabelAndParentId(movieId, kind, label, parentId);
        }
        if (labelExists) {
            throw new BadRequestException("[Movie Item] label existed", ErrorCode.MOVIE_ITEM_ERROR_LABEL_EXISTED);
        }
    }

    private void handleUpdateLatestMovieItem(MovieItem movieItem, Boolean isLatest) {
        if (Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_TRAILER)) {
            return;
        }

        Movie movie = movieItem.getMovie();
        boolean currentLatest = Boolean.TRUE.equals(movieItem.getIsLatest());
        boolean newLatest = Boolean.TRUE.equals(isLatest);
        boolean removeLatest = currentLatest && Boolean.FALSE.equals(isLatest);

        // not update latest -> update metadata if needed and return
        if (currentLatest && newLatest) {
            movieService.updateMetaDataMovie(movieItem);
            return;
        }

        // current is latest but update to not latest -> find next latest and update metadata
        if (removeLatest) {
            movieItem.setIsLatest(false);
            movieItemRepository.save(movieItem);

            if (Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SINGLE)
                    && Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON)) {
                movieItemRepository.findFirstByMovieIdAndKindAndIdNotOrderByOrderingDesc(
                        movie.getId(),
                        BaseConstant.MOVIE_ITEM_KIND_SEASON,
                        movieItem.getId()
                ).ifPresentOrElse(
                        nextLatestSeason -> {
                            nextLatestSeason.setIsLatest(true);
                            movieItemRepository.save(nextLatestSeason);
                            movieService.updateMetaDataMovie(nextLatestSeason);
                        },
                        () -> movieService.clearLatestMetadata(movie, true, false)
                );
                return;
            }

            if (Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SERIES)
                    && Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_EPISODE)) {

                movieItemRepository.findFirstByMovieIdAndKindAndIdNotOrderByOrderingDesc(
                        movie.getId(),
                        BaseConstant.MOVIE_ITEM_KIND_EPISODE,
                        movieItem.getId()
                ).ifPresentOrElse(
                        nextLatestEpisode -> {
                            nextLatestEpisode.setIsLatest(true);
                            movieItemRepository.save(nextLatestEpisode);

                            MovieItem nextParent = nextLatestEpisode.getParent();
                            if (nextParent != null && !Boolean.TRUE.equals(nextParent.getIsLatest())) {
                                movieItemRepository.resetLatest(
                                        movie.getId(),
                                        BaseConstant.MOVIE_ITEM_KIND_SEASON
                                );
                                nextParent.setIsLatest(true);
                                movieItemRepository.save(nextParent);
                            }

                            movieService.updateMetaDataMovie(nextLatestEpisode);
                        },
                        () -> {
                            movieItemRepository.resetLatest(
                                    movie.getId(),
                                    BaseConstant.MOVIE_ITEM_KIND_SEASON
                            );
                            movieService.clearLatestMetadata(movie, true, true);
                        }
                );
            }
            return;
        }

        // update to latest -> reset current latest and update metadata
        if (newLatest) {
            if (Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SINGLE)
                    && Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_SEASON)) {
                movieItemRepository.resetLatest(
                        movie.getId(),
                        BaseConstant.MOVIE_ITEM_KIND_SEASON
                );
                movieItem.setIsLatest(true);
                movieItemRepository.save(movieItem);
                movieService.updateMetaDataMovie(movieItem);
                return;
            }

            if (Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SERIES)
                    && Objects.equals(movieItem.getKind(), BaseConstant.MOVIE_ITEM_KIND_EPISODE)) {
                movieItemRepository.resetLatest(
                        movie.getId(),
                        BaseConstant.MOVIE_ITEM_KIND_EPISODE
                );
                movieItem.setIsLatest(true);
                movieItemRepository.save(movieItem);

                MovieItem parent = movieItem.getParent();
                if (parent != null && !Boolean.TRUE.equals(parent.getIsLatest())) {
                    movieItemRepository.resetLatest(
                            movie.getId(),
                            BaseConstant.MOVIE_ITEM_KIND_SEASON
                    );
                    parent.setIsLatest(true);
                    movieItemRepository.save(parent);
                }

                movieService.updateMetaDataMovie(movieItem);
            }
        }
    }
}
