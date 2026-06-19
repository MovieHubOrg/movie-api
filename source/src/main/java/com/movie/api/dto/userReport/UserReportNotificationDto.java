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
}
