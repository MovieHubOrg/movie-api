package com.movie.api.dto.serverconfig;

import com.movie.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ServerConfigDto extends ABasicAdminDto {
    @ApiModelProperty(name = "serverNumber")
    private Integer serverNumber;

    @ApiModelProperty(name = "name")
    private String name;

    @ApiModelProperty(name = "hostname")
    private String hostname;

    @ApiModelProperty(name = "ip")
    private String ip;

    @ApiModelProperty(name = "port")
    private Integer port;
}
