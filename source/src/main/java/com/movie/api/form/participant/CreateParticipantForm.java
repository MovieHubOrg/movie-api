package com.movie.api.form.participant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ApiModel
public class CreateParticipantForm {
    @NotNull(message = "roomId cannot be null")
    @ApiModelProperty(required = true)
    private Long roomId;

    @NotNull(message = "accountIds cannot be null")
    @Size(min = 1, message = "accountIds must have at least 1 account")
    @ApiModelProperty(required = true)
    private List<@NotNull Long> accountIds;
}
