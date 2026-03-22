package com.movie.api.dto.account;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.LongToStringIfWebSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class AccountDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long id;
    private String fullName;
    private String avatarPath;
    private Integer kind;
}
