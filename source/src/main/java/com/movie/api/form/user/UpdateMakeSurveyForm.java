package com.movie.api.form.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class UpdateMakeSurveyForm {
    @NotNull
    @ApiModelProperty(required = true)
    private Long userId;

    @NotNull
    @ApiModelProperty(required = true)
    private Boolean isMakeSurvey;
}
