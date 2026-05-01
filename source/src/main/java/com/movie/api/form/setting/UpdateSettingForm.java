package com.movie.api.form.setting;

import com.movie.api.validation.SettingDataTypeConstraint;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateSettingForm {
    @NotNull(message = "id cannot be null")
    @ApiModelProperty(name = "id", required = true)
    private Long id;
    @NotBlank(message = "valueData cannot be null")
    @ApiModelProperty(name = "valueData", required = true)
    private String valueData;
    @NotBlank(message = "groupName cannot be null")
    @ApiModelProperty(name = "groupName", required = true)
    private String groupName;
    @NotBlank(message = "description cannot be null")
    @ApiModelProperty(name = "description", required = true)
    private String description;
    @NotBlank(message = "keyName cannot be null")
    @ApiModelProperty(name = "keyName", required = true)
    private String keyName;
    @NotBlank(message = "dataType cannot be null")
    @SettingDataTypeConstraint
    @ApiModelProperty(name = "dataType", required = true)
    private String dataType;
    @ApiModelProperty(name = "options")
    private String options;
}
