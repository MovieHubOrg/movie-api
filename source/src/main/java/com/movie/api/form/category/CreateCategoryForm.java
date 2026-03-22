package com.movie.api.form.category;

import com.movie.api.validation.StatusConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class CreateCategoryForm {
    @NotBlank(message = "name cannot be empty")
    @ApiModelProperty(required = true)
    private String name;

    @StatusConstraint
    @ApiModelProperty(required = true)
    private Integer status;
}
