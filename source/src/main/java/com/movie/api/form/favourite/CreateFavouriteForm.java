package com.movie.api.form.favourite;

import com.movie.api.validation.FavouriteTypeConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateFavouriteForm {
    @FavouriteTypeConstraint
    @ApiModelProperty(required = true)
    private Integer type;

    @NotNull(message = "targetId cannot be null")
    @ApiModelProperty(required = true)
    private Long targetId;
}
