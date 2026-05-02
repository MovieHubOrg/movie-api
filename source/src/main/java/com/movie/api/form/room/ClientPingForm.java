package com.movie.api.form.room;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class ClientPingForm {
    @NotNull(message = "accountId cannot be null")
    @ApiModelProperty(required = true)
    private Long accountId;
}
