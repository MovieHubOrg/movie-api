package com.movie.api.dto.appVersion;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class CheckAppVersionDto {
    private Boolean updateRequired;
    private Boolean forceUpdate;
    private AppVersionDto latestVersion;
}
