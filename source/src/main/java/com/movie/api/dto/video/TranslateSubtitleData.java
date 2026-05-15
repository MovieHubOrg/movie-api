package com.movie.api.dto.video;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranslateSubtitleData {
    private Long videoId;
    private Long subtitleId;
    private String sourceLang;
    private String destLang;
    private String fileUrl;
}
