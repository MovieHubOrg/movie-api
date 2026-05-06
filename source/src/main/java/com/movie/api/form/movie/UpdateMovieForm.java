package com.movie.api.form.movie;

import com.movie.api.validation.AgeRatingConstraint;
import com.movie.api.validation.StatusConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ApiModel
public class UpdateMovieForm {
    @NotNull(message = "id cannot be empty")
    @ApiModelProperty(required = true)
    private Long id;

    @NotBlank(message = "title cannot be empty")
    @ApiModelProperty(required = true)
    private String title;

    @NotBlank(message = "originalTitle cannot be empty")
    @ApiModelProperty(required = true)
    private String originalTitle;

    @NotBlank(message = "description cannot be empty")
    @ApiModelProperty(required = true)
    private String description;

    @NotBlank(message = "thumbnailUrl cannot be empty")
    @ApiModelProperty(required = true)
    private String thumbnailUrl;

    @NotBlank(message = "posterUrl cannot be empty")
    @ApiModelProperty(required = true)
    private String posterUrl;

    @ApiModelProperty
    private String imageTitleUrl;

    @NotNull(message = "releaseDate cannot be null")
    @ApiModelProperty(required = true)
    private Date releaseDate;

    @NotNull(message = "isFeatured cannot be empty")
    @ApiModelProperty(required = true)
    private Boolean isFeatured;

    @ApiModelProperty
    private String language;

    @ApiModelProperty
    private String country;

    @AgeRatingConstraint
    @ApiModelProperty(required = true)
    private Integer ageRating;

    @NotNull(message = "year cannot be null")
    @ApiModelProperty(required = true)
    private Integer year;

    @Pattern(regexp = "^$|^tt\\d{7,12}$", message = "imdbId is invalid")
    @ApiModelProperty
    private String imdbId;

    @Min(1)
    @ApiModelProperty
    private Long duration;

    @ApiModelProperty
    private List<@NotNull Long> categoryIds;

    @StatusConstraint
    @ApiModelProperty(required = true)
    private Integer status;
}
