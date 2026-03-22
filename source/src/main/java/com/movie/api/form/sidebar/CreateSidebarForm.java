package com.movie.api.form.sidebar;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Getter
@Setter
@ApiModel
public class CreateSidebarForm {
    @NotNull(message = "movieId cannot be null")
    @ApiModelProperty(required = true)
    private Long movieId;

    private String description;

    private String webThumbnailUrl;

    private String mobileThumbnailUrl;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "mainColor must be in hex format")
    private String mainColor;

    private Boolean active;
}
