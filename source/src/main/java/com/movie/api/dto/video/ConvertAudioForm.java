package com.movie.api.dto.video;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConvertAudioForm {
    private Long videoId;
    private String content;
    private Integer sourceType;
}
