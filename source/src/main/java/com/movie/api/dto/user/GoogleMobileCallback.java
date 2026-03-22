package com.movie.api.dto.user;

import com.movie.api.validation.PlatformConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class GoogleMobileCallback {
    @NotNull(message = "idToken cannot be null")
    @ApiModelProperty(required = true)
    private String idToken;

    @PlatformConstraint
    @ApiModelProperty(required = true)
    private Integer platform;
}
