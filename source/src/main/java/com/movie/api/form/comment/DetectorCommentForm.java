package com.movie.api.form.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class DetectorCommentForm {
    @JsonProperty("comment_id")
    private Long commentId;
    private String content;

    /**
     * 1 = comment, 2 = review
     */
    @JsonProperty("type")
    private Integer type;

    /**
     * Scan token echoed back by the detector so a stale reply (for content the
     * user has since edited again) can be discarded. See CommentService.
     */
    @JsonProperty("scan_version")
    private Integer scanVersion;
}
