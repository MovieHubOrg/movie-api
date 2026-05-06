package com.movie.api.form.movie;

import com.movie.api.form.notification.SendNotificationConfigForm;
import com.movie.api.validation.AgeRatingConstraint;
import com.movie.api.validation.MovieTypeConstraint;
import com.movie.api.validation.StatusConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ApiModel
public class CreateMovieForm {
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

    @MovieTypeConstraint
    @ApiModelProperty(required = true)
    private Integer type;

    private Boolean isFeatured;

    private String language;

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

    @NotNull(message = "categoryIds cannot be null")
    @NotEmpty(message = "categoryIds cannot be empty")
    @ApiModelProperty(required = true)
    private List<@NotNull Long> categoryIds;

    @StatusConstraint
    @ApiModelProperty(required = true)
    private Integer status;

    @Valid
    @NotNull(message = "sendNotificationConfig cannot be null")
    @ApiModelProperty(required = true)
    private SendNotificationConfigForm sendNotificationConfig;
}
