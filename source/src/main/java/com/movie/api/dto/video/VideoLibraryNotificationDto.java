package com.movie.api.dto.video;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class VideoLibraryNotificationDto {
    private String id;
    private String name;
    private Long duration;
    private Integer state;
    private String thumbnailUrl;
}
