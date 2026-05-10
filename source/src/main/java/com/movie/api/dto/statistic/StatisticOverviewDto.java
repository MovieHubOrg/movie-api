package com.movie.api.dto.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticOverviewDto {
    private Long totalUsers;
    private Long totalMovies;
    private Long totalSingleMovies;
    private Long totalSeriesMovies;
    private Long totalViews;
    private Long totalComments;
    private Long totalReviews;
    private Long totalFavourites;
    private Double averageRating;
}
