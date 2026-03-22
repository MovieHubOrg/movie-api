package com.movie.api.form.video;

import com.movie.api.validation.StatusConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class UpdateVideoLibraryForm extends ExternalVideoLibraryForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;

    @NotBlank(message = "name cannot be empty")
    @ApiModelProperty(required = true)
    private String name;

    private String content;

    @NotBlank(message = "description cannot be empty")
    @ApiModelProperty(required = true)
    private String description;

    private String thumbnailUrl;

    private Long introStart;

    private Long introEnd;

    private Long outroStart;

    @StatusConstraint
    @ApiModelProperty(required = true)
    private Integer status;
}
