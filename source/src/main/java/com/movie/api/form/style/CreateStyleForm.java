package com.movie.api.form.style;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateStyleForm {
    @NotNull(message = "type cannot be null")
    @ApiModelProperty(required = true)
    private Integer type;

    @NotBlank(message = "name cannot be empty")
    @ApiModelProperty(required = true)
    private String name;

    private String description;

    @NotNull(message = "imageMobileUrl cannot be null")
    @ApiModelProperty(required = true)
    private String imageMobileUrl;

    @NotNull(message = "imageWebUrl cannot be null")
    @ApiModelProperty(required = true)
    private String imageWebUrl;

    @NotNull(message = "isDefault cannot be null")
    @ApiModelProperty(required = true)
    private Boolean isDefault;
}