package com.movie.api.dto.video;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class VideoLibrarySubtitleNotificationDto {
    private String videoLibraryId;
    private String name;
    private String thumbnailUrl;
    private Integer sourceType;
    private String id;
    private String language;
    private String label;
    private Integer state;
}
