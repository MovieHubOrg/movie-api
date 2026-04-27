package com.movie.api.dto.setting;

import com.movie.api.dto.ABasicAdminDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingDto extends ABasicAdminDto {
    private String groupName;
    private String description;
    private String keyName;
    private String valueData;
    private String dataType;
    private String options;
    private Boolean isSystem;
}
