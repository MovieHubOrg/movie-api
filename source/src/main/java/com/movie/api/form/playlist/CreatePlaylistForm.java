package com.movie.api.form.playlist;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class CreatePlaylistForm {
    @NotBlank(message = "name cannot be empty")
    @ApiModelProperty(required = true)
    private String name;
}