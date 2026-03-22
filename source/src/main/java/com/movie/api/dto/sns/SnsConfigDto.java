package com.movie.api.dto.sns;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.LongToStringIfWebSerializer;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class SnsConfigDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long applicationId;
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long applicationChannelId;
    private String secretKey;
}
