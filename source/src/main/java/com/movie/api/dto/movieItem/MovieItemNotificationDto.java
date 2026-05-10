package com.movie.api.dto.movieItem;

import com.movie.api.dto.movie.MovieNotificationDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@ApiModel
public class MovieItemNotificationDto {
    private String id;
    private String title;
    private Integer kind;
    private String label;
    private MovieNotificationDto movie;
    private Date releaseDate;
    private String thumbnailUrl;
}
