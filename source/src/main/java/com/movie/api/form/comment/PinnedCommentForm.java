package com.movie.api.form.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class PinnedCommentForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;

    @NotNull(message = "isPinned cannot be null")
    @ApiModelProperty(required = true)
    private Boolean isPinned;
}
