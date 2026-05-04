package com.movie.api.dto.movie;

import com.movie.api.dto.category.CategoryDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
public class RecentWatchedCategoryRecommendationDto {
    private CategoryDto category;
    private List<MovieDto> movies;
}
