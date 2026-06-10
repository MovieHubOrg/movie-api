package com.movie.api.form.video;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoneTranslateSubtitleForm {
    private Long subtitleId;
    private String fileUrl;
    private Integer state;
}
