package com.movie.api.form.watchHistory;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class TrackingWatchHistoryForm {
    @NotNull(message = "movieItemId cannot be null")
    @ApiModelProperty(required = true)
    private Long movieItemId;

    @NotNull(message = "lastWatchSeconds cannot be empty")
    @Min(0)
    @ApiModelProperty(required = true)
    private Long lastWatchSeconds;
}