package com.movie.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ABasicAdminDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    @ApiModelProperty(name = "id")
    private Long id;

    @ApiModelProperty(name = "status")
    private Integer status;

    @ApiModelProperty(name = "modifiedDate")
    private Date modifiedDate;

    @ApiModelProperty(name = "createdDate")
    private Date createdDate;
}
