package com.movie.api.form.room;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.movie.api.validation.RoomKindConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ApiModel
public class CreateRoomForm {
    @NotBlank(message = "name cannot be blank")
    @ApiModelProperty(required = true)
    private String name;

    @NotNull(message = "movieItemId cannot be null")
    @ApiModelProperty(required = true)
    private Long movieItemId;

    @RoomKindConstraint
    @ApiModelProperty(required = true)
    private Integer kind;

    @Future(message = "startTime must be in the future")
    @NotNull(message = "startTime cannot be null")
    @ApiModelProperty(required = true)
    private Date startTime;

    @Future(message = "endTime must be in the future")
    @ApiModelProperty
    private Date endTime;

    @ApiModelProperty(name = "accountIds")
    private List<@NotNull Long> accountIds;
}
