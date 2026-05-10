package com.movie.api.form.room.mqtt;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class ParticipantLeftForm {
    @NotBlank(message = "accountId cannot be blank")
    private String accountId;
}
