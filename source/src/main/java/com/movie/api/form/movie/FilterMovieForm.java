package com.movie.api.form.movie;

import com.movie.api.validation.AgeRatingConstraint;
import com.movie.api.validation.MovieTypeConstraint;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import java.util.List;

@Getter
@Setter
@ApiModel
public class FilterMovieForm {
    @MovieTypeConstraint(allowNull = true)
    private Integer type;

    @AgeRatingConstraint(allowNull = true)
    private Integer ageRating;

    private String language;

    private String country;

    private Boolean isFeatured;

    private List<Long> categoryIds;

    private Boolean comingSoon;

    private Boolean topImdb;

    @Min(value = 1)
    private Integer limit;
}
