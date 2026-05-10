package com.movie.api.dto.account;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class AccountNotificationDto {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String avatarPath;
}
