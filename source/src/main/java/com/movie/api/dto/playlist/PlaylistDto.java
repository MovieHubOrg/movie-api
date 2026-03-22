package com.movie.api.dto.playlist;

import com.movie.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class PlaylistDto extends ABasicAdminDto {
    private String name;
    private Integer totalMovie;
}
