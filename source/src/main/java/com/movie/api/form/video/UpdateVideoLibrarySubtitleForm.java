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
public class UpdateVideoLibrarySubtitleForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;

    @ApiModelProperty
    @NotBlank(message = "label cannot be blank")
    private String label;

    @ApiModelProperty
    @NotNull(message = "isDefault cannot be null")
    private Boolean isDefault;
}
