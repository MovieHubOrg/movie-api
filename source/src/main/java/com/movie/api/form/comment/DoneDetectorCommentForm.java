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

    private Integer type;

    /**
     * Echoed back from the detect request; used to drop stale replies when the
     * content was edited again while a scan was in flight. May be null if the
     * detector has not yet been updated to echo it.
     */
    @JsonProperty("scan_version")
    private Integer scanVersion;
}
