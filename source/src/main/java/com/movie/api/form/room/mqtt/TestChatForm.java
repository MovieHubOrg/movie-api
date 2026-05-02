package com.movie.api.form.room.mqtt;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class TestChatForm {
    @NotNull(message = "roomId cannot be null")
    private Long roomId;

    @NotNull(message = "accountId cannot be null")
    private Long accountId;

    @NotBlank(message = "content cannot be null")
    private String content;
}
