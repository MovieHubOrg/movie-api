package com.movie.api.dto.movie;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
public class SuggestByWatchedDto {
    private MovieDto referenceMovie;
    private List<MovieDto> suggestedMovies;
}
