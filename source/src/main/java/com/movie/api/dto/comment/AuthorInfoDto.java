package com.movie.api.dto.comment;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.LongToStringIfWebSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class AuthorInfoDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long id;
    private String email;
    private String fullName;
    private Integer kind;
    private String avatarPath;
    private Integer gender;
}
