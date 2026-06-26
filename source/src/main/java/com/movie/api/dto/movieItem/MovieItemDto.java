package com.movie.api.dto.movieItem;

import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.dto.video.VideoLibraryDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ApiModel
public class MovieItemDto extends ABasicAdminDto {
    private String title;
    private String description;
    private Integer kind;
    private String label;
    private Integer ordering;
    private MovieItemDto parent;
    private MovieDto movie;
    private VideoLibraryDto video;
    private Date releaseDate;
    private String thumbnailUrl;
    private Integer totalEpisode;
    private Boolean isLatest;

    private List<MovieItemDto> episodes;
    private MovieItemDto trailer;
    private MovieItemDto season;
}
