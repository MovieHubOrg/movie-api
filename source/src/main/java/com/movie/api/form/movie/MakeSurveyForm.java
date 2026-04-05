package com.movie.api.form.movie;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ApiModel
public class MakeSurveyForm {
    @NotNull(message = "movieIds cannot be empty")
    @Size(min = 3, message = "movieIds must have at least 3 movies")
    @ApiModelProperty(required = true)
    private List<Long> movieIds;
}
