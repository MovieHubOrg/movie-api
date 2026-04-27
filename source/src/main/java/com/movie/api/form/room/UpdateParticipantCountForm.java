package com.movie.api.form.room;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class UpdateParticipantCountForm {
    private Long roomId;
    private Integer currentViewers;
}
