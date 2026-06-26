package com.movie.api.form.room;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ApiModel
public class AddParticipantForm {
    @NotNull(message = "roomId cannot be null")
    @ApiModelProperty(required = true)
    private Long roomId;

    @NotEmpty(message = "accountIds cannot be empty")
    @ApiModelProperty(required = true)
    private List<@NotNull Long> accountIds;
}
