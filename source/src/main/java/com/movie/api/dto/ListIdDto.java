package com.movie.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
@AllArgsConstructor
public class ListIdDto {
    @JsonSerialize(contentUsing = LongToStringIfWebSerializer.class)
    private List<Long> ids;
}
