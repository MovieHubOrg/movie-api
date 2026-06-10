package com.movie.api.form.video;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoneProcessSubtitleForm {
    private Long videoId;
    private Integer state;
    private String reason;
    private String language;
    private String fileUrl;
}
