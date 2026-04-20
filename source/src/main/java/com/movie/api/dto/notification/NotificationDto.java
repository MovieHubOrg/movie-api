package com.movie.api.dto.notification;

import com.movie.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class NotificationDto extends ABasicAdminDto {
    private String title;
    private String cmd;
    private String body;
    private Integer type;
    private Boolean isRead = false;
}
