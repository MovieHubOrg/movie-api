package com.movie.api.form.appVersion;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateAppVersionForm {
    @NotNull(message = "code cannot be null")
    @ApiModelProperty(required = true)
    private Integer code;

    @NotBlank(message = "name cannot be empty")
    @ApiModelProperty(required = true)
    private String name;

    @NotBlank(message = "filePath cannot be empty")
    @ApiModelProperty(required = true)
    private String filePath;

    @NotNull(message = "forceUpdate cannot be null")
    @ApiModelProperty(required = true)
    private Boolean forceUpdate;

    @NotBlank(message = "changeLog cannot be empty")
    @ApiModelProperty(required = true)
    private String changeLog;

    @NotNull(message = "isLatest cannot be null")
    @ApiModelProperty(required = true)
    private Boolean isLatest;
}