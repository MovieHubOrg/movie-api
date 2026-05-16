package com.movie.api.dto.video;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteSubtitleForm {
    private Long videoId;
    private String fileUrl;
}
