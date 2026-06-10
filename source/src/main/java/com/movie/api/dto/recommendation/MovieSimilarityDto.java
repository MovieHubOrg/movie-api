package com.movie.api.dto.recommendation;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class MovieSimilarityDto {
    private Long movieId;
    private Long similarMovieId;
    private Double score;
    private String reason;
    private String modelVersion;
}
