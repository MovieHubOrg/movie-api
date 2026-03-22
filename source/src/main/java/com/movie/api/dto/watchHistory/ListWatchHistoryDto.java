package com.movie.api.dto.watchHistory;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
public class ListWatchHistoryDto {
    private Boolean isCompletedMovie;
    private List<WatchHistoryDto> watchHistories;
}
