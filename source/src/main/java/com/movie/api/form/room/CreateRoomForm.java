package com.movie.api.form.room;

import com.movie.api.validation.RoomKindConstraint;
import com.movie.api.validation.ValidStartTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ApiModel
@ValidStartTime
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

    @NotNull(message = "isStartNow cannot be null")
    @ApiModelProperty(required = true)
    private Boolean isStartNow;

    private Date startTime;

    @ApiModelProperty(name = "accountIds")
    private List<@NotNull Long> accountIds;
}
