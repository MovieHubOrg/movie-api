package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ErrorCode;
import com.movie.api.dto.watchHistory.ListWatchHistoryDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.exception.NotFoundException;
import com.movie.api.form.watchHistory.TrackingWatchHistoryForm;
import com.movie.api.mapper.WatchHistoryMapper;
import com.movie.api.service.UserMovieService;
import com.movie.api.storage.criteria.WatchHistoryCriteria;
import com.movie.api.storage.model.*;
import com.movie.api.storage.repository.AccountRepository;
import com.movie.api.storage.repository.MovieItemRepository;
import com.movie.api.storage.repository.WatchHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/watch-history")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class WatchHistoryController extends ABasicController {
    private static final long REWIND_SECONDS = 15L;

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

    @Autowired
    private WatchHistoryMapper watchHistoryMapper;

    @Autowired
    private MovieItemRepository movieItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserMovieService userMovieService;

    @Transactional
    @PostMapping(value = "/tracking", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> tracking(@Valid @RequestBody TrackingWatchHistoryForm form) {
        Account user = accountRepository.findByIdAndStatusAndKind(getCurrentUser(), BaseConstant.STATUS_ACTIVE, BaseConstant.ACCOUNT_KIND_USER)
                .orElseThrow(() -> new NotFoundException("[User] not found", ErrorCode.USER_ERROR_NOT_FOUND));

        MovieItem movieItem = movieItemRepository.findByIdAndKindNot(form.getMovieItemId(), BaseConstant.MOVIE_ITEM_KIND_TRAILER)
                .orElseThrow(() -> new NotFoundException("[MovieItem] not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND));

        if (movieItem.getVideo() == null) {
            throw new BadRequestException("[Video] not found", ErrorCode.MOVIE_ITEM_ERROR_NOT_FOUND);
        }

        WatchHistory watchHistory = watchHistoryRepository.findByMovieItemIdAndUserId(movieItem.getId(), user.getId()).orElse(null);
        if (watchHistory == null) {
            watchHistory = new WatchHistory();
            watchHistory.setUser(user);
            watchHistory.setMovie(movieItem.getMovie());
            watchHistory.setMovieItem(movieItem);
        }
        watchHistory.setStatus(BaseConstant.STATUS_ACTIVE);

        Long endOfVideo = movieItem.getVideo().getOutroStart() != null
                ? movieItem.getVideo().getOutroStart()
                : movieItem.getVideo().getDuration();

        Long adjustedWatchSeconds = Math.max(0L, form.getLastWatchSeconds() - REWIND_SECONDS);
        watchHistory.setLastWatchSeconds(adjustedWatchSeconds);
        if (watchHistory.getIsCompleted()) {
            if (form.getLastWatchSeconds() < endOfVideo) {
                watchHistory.setIsCompleted(false);
            }
        } else if (form.getLastWatchSeconds() >= endOfVideo) { // completed watch
            watchHistory.setIsCompleted(true);
            watchHistory.setTimesWatched(watchHistory.getTimesWatched() + 1);
        }
        watchHistoryRepository.save(watchHistory);
        saveWatchProgress(user.getId(), movieItem, adjustedWatchSeconds, endOfVideo);

        WatchHistory movieWatchHistory = watchHistoryRepository.findWatchHistoryMovie(movieItem.getMovie().getId(), user.getId()).orElse(null);
        if (movieWatchHistory == null) {
            movieWatchHistory = new WatchHistory();
            movieWatchHistory.setUser(user);
            movieWatchHistory.setMovie(movieItem.getMovie());
        }
        movieWatchHistory.setStatus(BaseConstant.STATUS_ACTIVE);

        boolean isCompletedMovie = checkCompletedMovie(movieWatchHistory);
        if (isCompletedMovie && !movieWatchHistory.getIsCompleted()) {
            movieWatchHistory.setTimesWatched(movieWatchHistory.getTimesWatched() + 1);
            saveWatchedUserMovie(user.getId(), movieItem.getMovie().getId());
        }
        movieWatchHistory.setIsCompleted(isCompletedMovie);
        watchHistoryRepository.save(movieWatchHistory);
        return makeSuccessResponse("Tracking watch history success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ListWatchHistoryDto> list(@Param("movieId") Long movieId) {
        WatchHistoryCriteria criteria = new WatchHistoryCriteria();
        criteria.setUserId(getCurrentUser());
        criteria.setMovieId(movieId);
        criteria.setStatus(BaseConstant.STATUS_ACTIVE);

        List<WatchHistory> watchHistories = watchHistoryRepository.findAll(criteria.getSpecification());

        // check completed movie -> check isCompleted WatchHistory with null movieItem
        // remove this WatchHistory of Movie when response
        boolean isCompletedMovie = false;
        Iterator<WatchHistory> iterator = watchHistories.iterator();
        while (iterator.hasNext()) {
            WatchHistory watchHistory = iterator.next();
            if (watchHistory.getMovieItem() == null) {
                isCompletedMovie = Boolean.TRUE.equals(watchHistory.getIsCompleted());
                iterator.remove();
                break;
            }
        }

        // sort asc by modifiedDate
        watchHistories = watchHistories.stream()
                .sorted(Comparator.comparing(WatchHistory::getModifiedDate).reversed())
                .collect(Collectors.toList());

        ListWatchHistoryDto listWatchHistoryDto = new ListWatchHistoryDto();
        listWatchHistoryDto.setIsCompletedMovie(isCompletedMovie);
        listWatchHistoryDto.setWatchHistories(watchHistoryMapper.fromEntityToWatchHistoryDtoList(watchHistories));
        return makeSuccessResponse(listWatchHistoryDto, "List watch movie success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> delete(@PathVariable("movieId") Long movieId) {
        Long userId = getCurrentUser();
        watchHistoryRepository.softDeleteByUserIdAndMovieId(BaseConstant.STATUS_DELETE, userId, movieId);
        userMovieService.deleteSignal(
                userId,
                movieId,
                BaseConstant.USER_MOVIE_TYPE_WATCHED,
                BaseConstant.USER_MOVIE_SOURCE_WATCH_HISTORY
        );
        userMovieService.deleteSignal(
                userId,
                movieId,
                BaseConstant.USER_MOVIE_TYPE_WATCH_PROGRESS,
                BaseConstant.USER_MOVIE_SOURCE_WATCH_HISTORY
        );
        return makeSuccessResponse("Delete watch history success");
    }

    private boolean checkCompletedMovie(WatchHistory movieWatchHistory) {
        Movie movie = movieWatchHistory.getMovie();
        Account user = movieWatchHistory.getUser();
        Integer kind = Objects.equals(movie.getType(), BaseConstant.MOVIE_TYPE_SINGLE)
                ? BaseConstant.MOVIE_ITEM_KIND_SEASON
                : BaseConstant.MOVIE_ITEM_KIND_EPISODE;
        Long targetTotal = movieItemRepository.countByMovieIdAndKind(movie.getId(), kind);
        Long totalCompleted = watchHistoryRepository.countCompletedWatchHistory(movie.getId(), user.getId(), BaseConstant.STATUS_ACTIVE);
        return Objects.equals(totalCompleted, targetTotal);
    }

    private void saveWatchProgress(Long userId, MovieItem movieItem, Long lastWatchSeconds, Long endOfVideo) {
        Long movieId = movieItem.getMovie() != null ? movieItem.getMovie().getId() : null;
        Double progressValue = userMovieService.calculateWatchProgressValue(lastWatchSeconds, endOfVideo);
        if (progressValue <= 0) {
            userMovieService.deleteSignal(
                    userId,
                    movieId,
                    BaseConstant.USER_MOVIE_TYPE_WATCH_PROGRESS,
                    BaseConstant.USER_MOVIE_SOURCE_WATCH_HISTORY
            );
            return;
        }

        userMovieService.saveSignal(
                userId,
                movieId,
                movieItem.getId(),
                BaseConstant.USER_MOVIE_TYPE_WATCH_PROGRESS,
                BaseConstant.USER_MOVIE_SOURCE_WATCH_HISTORY,
                progressValue
        );
    }

    private void saveWatchedUserMovie(Long userId, Long movieId) {
        userMovieService.saveSignal(
                userId,
                movieId,
                null,
                BaseConstant.USER_MOVIE_TYPE_WATCHED,
                BaseConstant.USER_MOVIE_SOURCE_WATCH_HISTORY,
                1.0
        );
    }
}
