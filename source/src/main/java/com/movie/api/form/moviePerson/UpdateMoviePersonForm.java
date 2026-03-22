package com.movie.api.form.moviePerson;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class UpdateMoviePersonForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;

    @NotNull(message = "kind cannot be null")
    @ApiModelProperty(required = true)
    private Integer kind;

    @ApiModelProperty
    private String characterName;

}
