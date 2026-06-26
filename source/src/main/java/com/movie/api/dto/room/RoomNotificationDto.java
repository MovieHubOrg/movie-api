package com.movie.api.dto.room;

import com.movie.api.dto.account.AccountNotificationDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@ApiModel
public class RoomNotificationDto {
    private String id;
    private String code;
    private String name;
    private Integer kind;
    private String movieItemId;
    private String movieId;
    private String movieTitle;
    private String movieThumbnail;
    private Date startTime;
    private Date endTime;
    private AccountNotificationDto host;
}
