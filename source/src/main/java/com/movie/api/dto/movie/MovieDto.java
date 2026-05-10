package com.movie.api.dto.movie;

import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.category.CategoryDto;
import com.movie.api.dto.movieItem.MovieItemDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ApiModel
public class MovieDto extends ABasicAdminDto {
    private String title;
    private String originalTitle;
    private String slug;
    private String description;
    private String thumbnailUrl;
    private String posterUrl;
    private String imageTitleUrl;
    private Date releaseDate;
    private Integer year;
    private Integer type;
    private Boolean isFeatured;
    private String language;
    private String country;
    private Integer ageRating;
    private List<CategoryDto> categories;
    private Long viewCount;
    private Long commentCount;
    private Long reviewCount;
    private Double averageRating;
    private String imdbId;
    private Double imdbRating;
    private String metadata;
    private List<MovieItemDto> seasons;
}
