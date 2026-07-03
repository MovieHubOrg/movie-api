package com.movie.api.form.room.mqtt;

import com.movie.api.dto.participant.ParticipantDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
public class UpdateParticipantCountForm {
    private String roomId;
    private Integer currentViewers;
    private List<ParticipantDto> participants;
}
