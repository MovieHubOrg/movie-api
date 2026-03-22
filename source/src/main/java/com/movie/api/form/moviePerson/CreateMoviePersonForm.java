package com.movie.api.form.moviePerson;

import com.movie.api.validation.PersonKindConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateMoviePersonForm {

    @NotNull(message = "movieId cannot be null")
    @ApiModelProperty(required = true)
    private Long movieId;

    @NotNull(message = "personId cannot be null")
    @ApiModelProperty(required = true)
    private Long personId;

    @ApiModelProperty
    private String characterName;

    @PersonKindConstraint
    @ApiModelProperty(required = true)
    private Integer kind; // 1: Director, 2: Actor
}
