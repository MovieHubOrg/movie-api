package com.movie.api.dto.user;

import com.movie.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class UserDto extends ABasicAdminDto {
    @ApiModelProperty(name = "kind")
    private int kind;

    @ApiModelProperty(name = "username")
    private String username;

    @ApiModelProperty(name = "phone")
    private String phone;

    @ApiModelProperty(name = "email")
    private String email;

    @ApiModelProperty(name = "fullName")
    private String fullName;

    @ApiModelProperty(name = "avatarPath")
    private String avatarPath;

    @ApiModelProperty(name = "status")
    private Integer status;

    @ApiModelProperty(name = "gender")
    private Integer gender;
}
