package com.movie.api.dto.video;

import com.movie.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class VideoLibraryDto extends ABasicAdminDto {
    private String name;
    private String description;
    private Integer sourceType;
    private String content;
    private String relativeContentPath;
    private String spriteUrl;
    private String vttUrl;
    private String thumbnailUrl;
    private Long duration;
    private Long introStart;
    private Long introEnd;
    private Long outroStart;
    private Integer state;
    private String hostname;
}
