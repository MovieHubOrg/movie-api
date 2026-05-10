package com.movie.api.dto.movie;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ImdbRatingsSyncDto {
    private int matchedMovies;
    private int updatedMovies;
    private int skippedRows;
    private boolean dailyLimitReached;
    private int remainingRequests;
}
