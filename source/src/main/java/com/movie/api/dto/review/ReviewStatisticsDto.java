package com.movie.api.dto.review;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ReviewStatisticsDto {
    private Long reviewCount;
    private Double averageRating;
}
