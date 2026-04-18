package com.movie.api.form.notification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ApiModel
public class UpdateReadNotificationForm {
    @NotEmpty(message = "ids cannot be empty")
    @ApiModelProperty(required = true)
    private List<@NotNull Long> ids;
}
