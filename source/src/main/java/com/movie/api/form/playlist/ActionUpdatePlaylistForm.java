package com.movie.api.form.playlist;

import com.movie.api.validation.ActionPlaylistConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class ActionUpdatePlaylistForm {
    @NotNull(message = "playlistId cannot be empty")
    @ApiModelProperty(required = true)
    private Long playlistId;

    @ActionPlaylistConstraint
    @ApiModelProperty(required = true)
    private Integer action;
}