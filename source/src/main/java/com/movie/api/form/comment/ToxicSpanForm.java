package com.movie.api.form.comment;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ToxicSpanForm {
    private Integer start;
    private Integer end;
}
