package com.movie.api.dto.movie;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@ApiModel
public class MovieNotificationDto {
    private String id;
    private String title;
    private String originalTitle;
    private String slug;
    private String thumbnailUrl;
    private String posterUrl;
    private Date releaseDate;
}
