package com.movie.api.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class GoogleWebCallback {
    @NotNull(message = "code cannot be null")
    @ApiModelProperty(required = true)
    private String code;
}
