package com.movie.api.form.review;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class UpdateReviewForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;

    @Min(value = 1, message = "rate cannot be < 1")
    @Max(value = 5, message = "rate cannot be > 1")
    @ApiModelProperty(required = true)
    private Integer rate;

    @NotBlank(message = "content cannot be null")
    @ApiModelProperty(required = true)
    private String content;
}
