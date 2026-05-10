package com.movie.api.form.room.mqtt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class ClientPingForm {
    @NotBlank(message = "accountId cannot be blank")
    @ApiModelProperty(required = true)
    private String accountId;
}
