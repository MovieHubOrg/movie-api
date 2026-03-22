package com.movie.api.form.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class CreateCommentForm {
    private Long movieItemId;

    private Long movieId;

    @NotBlank(message = "content cannot be null")
    @ApiModelProperty(required = true)
    private String content;

    private Long parentId;

    private Long replyToId;

    private Integer replyToKind;

    @AssertTrue(message = "At least one of movieId or movieItemId must be provided.")
    public boolean isValidTarget() {
        return movieId != null || movieItemId != null;
    }
}
