package com.movie.api.form.notification;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.NotificationAudienceConstraint;
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

    @NotNull(message = "sendFor cannot be null")
    @NotificationAudienceConstraint
    @ApiModelProperty(required = true, notes = "1: all users, 2: interested users")
    private Integer sendFor = BaseConstant.SEND_NOTIFICATION_FOR_ALL_USERS;
}
