package com.movie.api.form.collection;

import com.movie.api.constant.BaseConstant;
import com.movie.api.form.movie.FilterMovieForm;
import com.movie.api.validation.CollectionTypeConstraint;
import com.movie.api.validation.ColorConstraint;
import com.movie.api.validation.ValidJsonField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ApiModel
public class UpdateCollectionForm {
    @NotNull(message = "id cannot be empty")
    @ApiModelProperty(required = true)
    private Long id;

    @NotBlank(message = "name cannot be empty")
    @ApiModelProperty(required = true)
    private String name;

    @ApiModelProperty(value = "List of hex colors for gradient", example = "[\"#FF5733\", \"#FFC300\"]", required = true)
    @NotNull(message = "colors cannot be null")
    @Size(min = 2, message = "gradientColors must contain at least 2 colors")
    @Valid
    private List<@ColorConstraint String> colors;

    private Long styleId;

    @CollectionTypeConstraint
    @ApiModelProperty(required = true)
    private Integer type;

    @ApiModelProperty(required = true, example = BaseConstant.FILTER_MOVIE_SAMPLE_DATA)
    @ValidJsonField(classType = FilterMovieForm.class)
    private String filter;
}