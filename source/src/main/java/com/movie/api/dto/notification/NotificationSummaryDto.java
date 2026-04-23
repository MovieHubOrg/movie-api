package com.movie.api.dto.notification;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.LongToStringIfWebSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@AllArgsConstructor
public class NotificationSummaryDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    @ApiModelProperty(name = "totalUnread")
    private Long totalUnread;
}
