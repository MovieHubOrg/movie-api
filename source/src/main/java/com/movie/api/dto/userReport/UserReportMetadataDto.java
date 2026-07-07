package com.movie.api.dto.userReport;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReportMetadataDto {
    private String movieItemId;
    private String movieId;
    private String movieTitle;
    private String movieThumbnail;
    private String parentId;

    private String videoId;
    private String videoName;
    private Long videoDuration;
    private Integer videoSourceType;
    private Integer videoState;
    private String videoThumbnailUrl;
}
