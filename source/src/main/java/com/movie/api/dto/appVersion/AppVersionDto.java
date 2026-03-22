package com.movie.api.dto.appVersion;

import com.movie.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class AppVersionDto extends ABasicAdminDto {
    private Integer code;
    private String name;
    private String filePath;
    private Boolean forceUpdate;
    private String changeLog;
    private Boolean isLatest;
}
