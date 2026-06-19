package com.movie.api.form.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class UpdateToxicSpansForm {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("toxic_spans")
    private String toxicSpans;
}
