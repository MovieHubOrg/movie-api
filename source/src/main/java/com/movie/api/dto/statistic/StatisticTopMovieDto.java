package com.movie.api.dto.statistic;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.LongToStringIfWebSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticTopMovieDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    @ApiModelProperty(name = "id")
    private Long id;
    private String title;
    private String thumbnailUrl;
    private Long viewCount;
    private Long commentCount;
    private Long reviewCount;
    private Double averageRating;
}
