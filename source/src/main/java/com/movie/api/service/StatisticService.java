package com.movie.api.service;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.statistic.StatisticGroupDto;
import com.movie.api.dto.statistic.StatisticOverviewDto;
import com.movie.api.dto.statistic.StatisticTopMovieDto;
import com.movie.api.exception.BadRequestException;
import com.movie.api.storage.repository.AccountRepository;
import com.movie.api.storage.repository.CommentRepository;
import com.movie.api.storage.repository.FavouriteRepository;
import com.movie.api.storage.repository.MovieRepository;
import com.movie.api.storage.repository.ReviewRepository;
import com.movie.api.storage.repository.WatchHistoryRepository;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class StatisticService {
    private static final Set<String> ALLOWED_TOP_MOVIE_SORTS = Set.of(
            "viewCount",
            "commentCount",
            "reviewCount",
            "averageRating"
    );

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

    public StatisticOverviewDto getOverview(Date fromDate, Date toDate) {
        Date normalizedFromDate = startOfDay(fromDate);
        Date normalizedToDate = endOfDay(toDate);
        validateDateRange(normalizedFromDate, normalizedToDate);

        Long totalUsers = accountRepository.countByStatusAndKindAndCreatedDateBetween(
                BaseConstant.STATUS_ACTIVE,
                BaseConstant.ACCOUNT_KIND_USER,
                normalizedFromDate,
                normalizedToDate
        );
        Long totalMovies = movieRepository.countStatisticMovies(BaseConstant.STATUS_ACTIVE, null, normalizedFromDate, normalizedToDate);
        Long totalSingleMovies = movieRepository.countStatisticMovies(BaseConstant.STATUS_ACTIVE, BaseConstant.MOVIE_TYPE_SINGLE, normalizedFromDate, normalizedToDate);
        Long totalSeriesMovies = movieRepository.countStatisticMovies(BaseConstant.STATUS_ACTIVE, BaseConstant.MOVIE_TYPE_SERIES, normalizedFromDate, normalizedToDate);
        Long totalViews = hasDateRange(normalizedFromDate, normalizedToDate)
                ? watchHistoryRepository.sumTimesWatchedByModifiedDateBetween(BaseConstant.STATUS_ACTIVE, normalizedFromDate, normalizedToDate)
                : movieRepository.sumViewCountByStatus(BaseConstant.STATUS_ACTIVE);
        Long totalComments = commentRepository.countByStatusAndCreatedDateBetween(BaseConstant.STATUS_ACTIVE, normalizedFromDate, normalizedToDate);
        Long totalReviews = reviewRepository.countByStatusAndCreatedDateBetween(BaseConstant.STATUS_ACTIVE, normalizedFromDate, normalizedToDate);
        Long totalFavourites = favouriteRepository.countByStatusAndCreatedDateBetween(BaseConstant.STATUS_ACTIVE, normalizedFromDate, normalizedToDate);
        Double averageRating = movieRepository.averageRatingByStatusAndCreatedDateBetween(BaseConstant.STATUS_ACTIVE, normalizedFromDate, normalizedToDate);

        return new StatisticOverviewDto(
                valueOrZero(totalUsers),
                valueOrZero(totalMovies),
                valueOrZero(totalSingleMovies),
                valueOrZero(totalSeriesMovies),
                valueOrZero(totalViews),
                valueOrZero(totalComments),
                valueOrZero(totalReviews),
                valueOrZero(totalFavourites),
                averageRating != null ? averageRating : 0.0
        );
    }

    public ResponseListDto<List<StatisticTopMovieDto>> getTopMovies(Date fromDate, Date toDate, String sortBy, int page, int size) {
        Date normalizedFromDate = startOfDay(fromDate);
        Date normalizedToDate = endOfDay(toDate);
        validateDateRange(normalizedFromDate, normalizedToDate);
        String resolvedSort = sortBy != null && ALLOWED_TOP_MOVIE_SORTS.contains(sortBy) ? sortBy : "viewCount";
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(Sort.Order.desc(resolvedSort), Sort.Order.desc("id"))
        );
        Page<StatisticTopMovieDto> movies = movieRepository.findTopMovieStatistics(
                BaseConstant.STATUS_ACTIVE,
                normalizedFromDate,
                normalizedToDate,
                pageable
        );
        return new ResponseListDto<>(movies.getContent(), movies.getTotalElements(), movies.getTotalPages());
    }

    public List<StatisticGroupDto> getMovieDistribution(String groupBy) {
        if ("type".equals(groupBy)) {
            return movieRepository.groupByType(BaseConstant.STATUS_ACTIVE);
        }
        if ("country".equals(groupBy)) {
            return movieRepository.groupByCountry(BaseConstant.STATUS_ACTIVE);
        }
        if ("language".equals(groupBy)) {
            return movieRepository.groupByLanguage(BaseConstant.STATUS_ACTIVE);
        }
        if ("ageRating".equals(groupBy)) {
            return movieRepository.groupByAgeRating(BaseConstant.STATUS_ACTIVE);
        }
        throw new BadRequestException("Invalid groupBy. Supported values: type, country, language, ageRating");
    }

    private Date startOfDay(Date date) {
        return date == null ? null : DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
    }

    private Date endOfDay(Date date) {
        if (date == null) {
            return null;
        }
        Date start = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
        return DateUtils.addMilliseconds(DateUtils.addDays(start, 1), -1);
    }

    private boolean hasDateRange(Date fromDate, Date toDate) {
        return fromDate != null || toDate != null;
    }

    private void validateDateRange(Date fromDate, Date toDate) {
        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            throw new BadRequestException("fromDate must be before or equal to toDate");
        }
    }

    private Long valueOrZero(Long value) {
        return value != null ? value : 0L;
    }
}
