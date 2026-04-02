package com.movie.api.form.serverconfig;

import com.movie.api.validation.StatusConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class UpdateServerConfigForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(required = true)
    private Long id;

    @NotNull(message = "serverNumber cannot be null")
    @ApiModelProperty(required = true)
    private Integer serverNumber;

    @NotBlank(message = "name cannot be empty")
    @ApiModelProperty(required = true)
    private String name;

    @NotBlank(message = "hostname cannot be empty")
    @ApiModelProperty(required = true)
    private String hostname;

    @NotBlank(message = "ip cannot be empty")
    @ApiModelProperty(required = true)
    private String ip;

    @NotNull(message = "port cannot be null")
    @ApiModelProperty(required = true)
    private Integer port;

    @StatusConstraint
    @ApiModelProperty(required = true)
    private Integer status;
}
