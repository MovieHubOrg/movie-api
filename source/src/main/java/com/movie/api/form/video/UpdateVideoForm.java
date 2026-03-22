package com.movie.api.form.video;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVideoForm {
    private Long id;
    private String content;
    private String relativeContentPath;
    private String spriteUrl;
    private String vttUrl;
    private Integer state;
    private Long duration;
}
