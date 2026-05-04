package com.movie.api.controller;

import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.statistic.StatisticGroupDto;
import com.movie.api.dto.statistic.StatisticOverviewDto;
import com.movie.api.dto.statistic.StatisticTopMovieDto;
import com.movie.api.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/statistic")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StatisticController extends ABasicController {
    @Autowired
    private StatisticService statisticService;

    @GetMapping(value = "/overview", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STAT_V')")
    public ApiMessageDto<StatisticOverviewDto> overview(
            @RequestParam(value = "fromDate", required = false) Date fromDate,
            @RequestParam(value = "toDate", required = false) Date toDate) {
        return makeSuccessResponse(statisticService.getOverview(fromDate, toDate), "Get statistic overview success");
    }

    @GetMapping(value = "/top-movies", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STAT_V')")
    public ApiMessageDto<ResponseListDto<List<StatisticTopMovieDto>>> topMovies(
            @RequestParam(value = "fromDate", required = false) Date fromDate,
            @RequestParam(value = "toDate", required = false) Date toDate,
            @RequestParam(value = "sortBy", defaultValue = "viewCount") String sortBy,
            Pageable pageable) {
        return makeSuccessResponse(
                statisticService.getTopMovies(fromDate, toDate, sortBy, pageable.getPageNumber(), pageable.getPageSize()),
                "Get top movies statistic success"
        );
    }

    @GetMapping(value = "/movie-distribution", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STAT_V')")
    public ApiMessageDto<List<StatisticGroupDto>> movieDistribution(
            @RequestParam(value = "groupBy", defaultValue = "type") String groupBy) {
        return makeSuccessResponse(statisticService.getMovieDistribution(groupBy), "Get movie distribution statistic success");
    }
}
