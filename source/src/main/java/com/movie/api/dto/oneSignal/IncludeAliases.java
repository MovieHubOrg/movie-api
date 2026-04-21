package com.movie.api.dto.oneSignal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel
public class IncludeAliases {
    @JsonProperty("external_id")
    private List<String> externalId;
}
