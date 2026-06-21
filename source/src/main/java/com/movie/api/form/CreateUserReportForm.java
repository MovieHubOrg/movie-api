package com.movie.api.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.movie.api.validation.UserReportTypeConstraint;

@Getter
@Setter
@ApiModel
public class CreateUserReportForm {
    @NotNull(message = "objectId is required")
    @ApiModelProperty(name = "objectId", required = true)
    private Long objectId;

    @UserReportTypeConstraint
    @ApiModelProperty(name = "type", required = true)
    private Integer type; // 1: comment, 2: review

    @NotBlank(message = "content is required")
    @ApiModelProperty(name = "content", required = true)
    private String content;
}
