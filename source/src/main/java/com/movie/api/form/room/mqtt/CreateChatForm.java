package com.movie.api.form.room.mqtt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel
public class CreateChatForm {
    @NotBlank(message = "accountId cannot be blank")
    @ApiModelProperty(required = true)
    private String accountId;

    @NotBlank(message = "content cannot be null")
    @ApiModelProperty(required = true)
    private String content;
}
