package com.movie.api.form.video;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class TranslateSubtitleForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;

    @NotBlank(message = "language cannot be blank")
    @ApiModelProperty(required = true)
    private String language;

    @NotBlank(message = "label cannot be blank")
    @ApiModelProperty(required = true)
    private String label;
}
