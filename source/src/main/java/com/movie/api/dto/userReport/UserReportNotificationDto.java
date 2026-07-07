package com.movie.api.dto.userReport;

import com.movie.api.dto.account.AccountNotificationDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReportNotificationDto {
    private String id;
    private AccountNotificationDto user;
    private String objectId;
    private Integer type;
    private String content;

    private String movieItemId;
    private String movieId;
    private String movieTitle;
    private String movieThumbnail;
    private String parentId;

    private String videoId;
    private String videoName;
    private Long videoDuration;
    private Integer videoSourceType;
    private Integer videoState;
    private String videoThumbnailUrl;
}
