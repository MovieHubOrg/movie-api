package com.movie.api.form.video;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAudioForm {
    private Long videoId;
    private String audioUrl;
    private Integer audioState;
    private String reason;
}
