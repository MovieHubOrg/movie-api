package com.movie.api.dto.watchHistory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.LongToStringIfWebSerializer;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.dto.movieItem.MovieItemDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class WatchHistoryDto extends ABasicAdminDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long movieItemId;
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long movieId;
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long userId;
    private Long lastWatchSeconds;
    private Boolean isCompleted;
    private Integer timesWatched;

    private MovieItemDto movieItem;
    private MovieDto movie;
}
