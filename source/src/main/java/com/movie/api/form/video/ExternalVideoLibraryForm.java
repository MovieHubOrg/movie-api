package com.movie.api.form.video;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ExternalVideoLibraryForm {
    private String vttUrl;
    private String spriteUrl;
    private Long duration;
}
