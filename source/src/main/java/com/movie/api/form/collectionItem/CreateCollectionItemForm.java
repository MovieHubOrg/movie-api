package com.movie.api.form.collectionItem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateCollectionItemForm {
    @NotNull(message = "collectionId cannot be empty")
    @ApiModelProperty(required = true)
    private Long collectionId;

    @NotNull(message = "movieId cannot be empty")
    @ApiModelProperty(required = true)
    private Long movieId;
}