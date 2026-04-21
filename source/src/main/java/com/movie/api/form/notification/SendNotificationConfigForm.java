package com.movie.api.form.notification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@ApiModel
public class SendNotificationConfigForm {
    @NotNull(message = "isSendNotification cannot be null")
    @ApiModelProperty(required = true)
    private Boolean isSendNotification;

    private Date scheduleAt = new Date();

    private String title;
}
