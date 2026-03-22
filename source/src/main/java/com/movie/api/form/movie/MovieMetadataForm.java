package com.movie.api.form.movie;

import com.movie.api.dto.movieItem.MovieItemDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class MovieMetadataForm {
    private MovieItemDto latestSeason;
    private MovieItemDto latestEpisode;
    private Long duration;
}
