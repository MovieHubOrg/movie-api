package com.movie.api.form.room.mqtt;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class EndRoomForm {
    private String roomId;
    private String reason;
}
