package com.movie.api.form.movieItem;

import com.movie.api.validation.StatusConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@ApiModel
public class UpdateMovieItemForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;

    @NotBlank(message = "title cannot be empty")
    @ApiModelProperty(required = true)
    private String title;

    @NotBlank(message = "label cannot be empty")
    @ApiModelProperty(required = true)
    private String label;

    @NotBlank(message = "description cannot be empty")
    @ApiModelProperty(required = true)
    private String description;

    @StatusConstraint
    @ApiModelProperty(required = true)
    private Integer status;

    @ApiModelProperty
    private Long videoId;

    @NotNull(message = "releaseDate cannot be null")
    @ApiModelProperty(required = true)
    private Date releaseDate;

    @ApiModelProperty
    private String thumbnailUrl;

    @ApiModelProperty
    @Min(value = 1)
    private Integer totalEpisode;
}
