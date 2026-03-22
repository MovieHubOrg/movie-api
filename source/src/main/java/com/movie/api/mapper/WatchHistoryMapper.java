package com.movie.api.mapper;

import com.movie.api.dto.watchHistory.WatchHistoryDto;
import com.movie.api.storage.model.WatchHistory;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MovieMapper.class, MovieItemMapper.class})
public interface WatchHistoryMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "movieItem.id", target = "movieItemId")
    @Mapping(source = "movie.id", target = "movieId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "lastWatchSeconds", target = "lastWatchSeconds")
    @Mapping(source = "isCompleted", target = "isCompleted")
    @Mapping(source = "timesWatched", target = "timesWatched")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToWatchHistoryDto")
    WatchHistoryDto fromEntityToWatchHistoryDto(WatchHistory watchHistory);

    @IterableMapping(elementTargetType = WatchHistoryDto.class, qualifiedByName = "fromEntityToWatchHistoryDto")
    List<WatchHistoryDto> fromEntityToWatchHistoryDtoList(List<WatchHistory> watchHistories);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "movieItem", target = "movieItem", qualifiedByName = "entityToMovieItemPublicDto")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieAutoCompleteDto")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "lastWatchSeconds", target = "lastWatchSeconds")
    @Mapping(source = "isCompleted", target = "isCompleted")
    @Mapping(source = "timesWatched", target = "timesWatched")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToWatchHistoryDetailsDto")
    WatchHistoryDto fromEntityToWatchHistoryDetailsDto(WatchHistory watchHistory);

    @IterableMapping(elementTargetType = WatchHistoryDto.class, qualifiedByName = "fromEntityToWatchHistoryDetailsDto")
    List<WatchHistoryDto> fromEntityToWatchHistoryDetailsDtoList(List<WatchHistory> watchHistories);
}
