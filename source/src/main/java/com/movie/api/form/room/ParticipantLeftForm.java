package com.movie.api.form.room;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class ParticipantLeftForm {
    @NotNull(message = "accountId cannot be null")
    private Long accountId;
}
