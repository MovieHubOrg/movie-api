package com.movie.api.dto.reaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.LongToStringIfWebSerializer;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel
public class VoteDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long id;
    private Integer type;
}
