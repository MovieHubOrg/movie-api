package com.movie.api.form.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
public class DoneDetectorCommentForm {
    @JsonProperty("comment_id")
    private Long commentId;

    @JsonProperty("toxic_spans")
    private List<ToxicSpanForm> toxicSpans;
}
