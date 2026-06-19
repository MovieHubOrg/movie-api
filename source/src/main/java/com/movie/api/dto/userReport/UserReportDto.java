package com.movie.api.dto.userReport;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.LongToStringIfWebSerializer;
import com.movie.api.dto.account.AccountDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReportDto extends ABasicAdminDto {
    private AccountDto user;

    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    @ApiModelProperty(name = "objectId")
    private Long objectId;

    private Integer type;

    private String content;
}
