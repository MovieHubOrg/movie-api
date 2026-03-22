package com.movie.api.form.movieItem;

import com.movie.api.validation.MovieItemKindConstraint;
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
public class CreateMovieItemForm {
    @NotBlank(message = "title cannot be empty")
    @ApiModelProperty(required = true)
    private String title;

    @NotBlank(message = "description cannot be empty")
    @ApiModelProperty(required = true)
    private String description;

    @MovieItemKindConstraint
    @ApiModelProperty(required = true)
    private Integer kind;

    @NotBlank(message = "label cannot be empty")
    @ApiModelProperty(required = true)
    private String label;

    @ApiModelProperty
    private Long parentId; // can be null

    @NotNull(message = "movieId cannot be null")
    @ApiModelProperty(required = true)
    private Long movieId;

    @ApiModelProperty
    private Long videoId; // can be null

    @StatusConstraint
    @ApiModelProperty(required = true)
    private Integer status;

    @NotNull(message = "releaseDate cannot be null")
    @ApiModelProperty(required = true)
    private Date releaseDate;

    @ApiModelProperty
    private String thumbnailUrl;

    @ApiModelProperty
    @Min(value = 1)
    private Integer totalEpisode;

    @ApiModelProperty
    private Boolean isLatest = false;
}
