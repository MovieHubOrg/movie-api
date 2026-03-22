package com.movie.api.form.playlist;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ApiModel
public class UpdatePlaylistItemForm {
    @NotNull(message = "movieId cannot be empty")
    @ApiModelProperty(required = true)
    private Long movieId;

    @NotNull(message = "playlistIds cannot be empty")
    @ApiModelProperty(required = true)
    private List<@Valid ActionUpdatePlaylistForm> actions;
}