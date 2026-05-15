package com.movie.api.dto.video;

import com.movie.api.dto.ABasicAdminDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class VideoLibrarySubtitleDto extends ABasicAdminDto {
    private Long videoLibraryId;
    private String language;
    private String label;
    private String fileUrl;
    private Boolean isDefault;
    private Integer state;
}
