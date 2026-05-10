package com.movie.api.mapper;

import com.movie.api.dto.movieItem.MovieItemDto;
import com.movie.api.dto.movieItem.MovieItemNotificationDto;
import com.movie.api.form.movieItem.CreateMovieItemForm;
import com.movie.api.form.movieItem.UpdateMovieItemForm;
import com.movie.api.storage.model.MovieItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MovieMapper.class, VideoLibraryMapper.class})
public interface MovieItemMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "parent", target = "parent", qualifiedByName = "entityToMovieItemParentDto")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieAutoCompleteDto")
    @Mapping(source = "video", target = "video", qualifiedByName = "entityToVideoLibraryShortDto")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "isLatest", target = "isLatest")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "totalEpisode", target = "totalEpisode")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieItemDto")
    MovieItemDto entityToMovieItemDto(MovieItem movieItem);

    @IterableMapping(elementTargetType = MovieItemDto.class, qualifiedByName = "entityToMovieItemDto")
    List<MovieItemDto> fromEntityToMovieItemDtoList(List<MovieItem> movieItems);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "parent", target = "parent", qualifiedByName = "entityToMovieItemParentDto")
    @Mapping(source = "video", target = "video", qualifiedByName = "entityToVideoLibraryShortDto")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "totalEpisode", target = "totalEpisode")
    @Mapping(source = "isLatest", target = "isLatest")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieItemAutoCompleteDto")
    MovieItemDto entityToMovieItemAutoCompleteDto(MovieItem movieItem);

    @IterableMapping(elementTargetType = MovieItemDto.class, qualifiedByName = "entityToMovieItemAutoCompleteDto")
    List<MovieItemDto> fromEntityToMovieItemAutoCompleteDtoList(List<MovieItem> movieItems);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieShortDto")
    @Mapping(source = "video", target = "video", qualifiedByName = "entityToVideoLibraryShortDto")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "totalEpisode", target = "totalEpisode")
    @Mapping(source = "isLatest", target = "isLatest")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieItemShortDto")
    MovieItemDto entityToMovieItemShortDto(MovieItem movieItem);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "totalEpisode", target = "totalEpisode")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieItemParentDto")
    MovieItemDto entityToMovieItemParentDto(MovieItem movieItem);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "parent", target = "parent", qualifiedByName = "entityToMovieItemParentDto")
    @Mapping(source = "video", target = "video", qualifiedByName = "entityToVideoLibraryShortDto")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "totalEpisode", target = "totalEpisode")
    @Mapping(source = "isLatest", target = "isLatest")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieItemPublicDto")
    MovieItemDto entityToMovieItemPublicDto(MovieItem movieItem);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieAutoCompleteDto")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "totalEpisode", target = "totalEpisode")
    @Mapping(source = "isLatest", target = "isLatest")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieItemDtoWithMovie")
    MovieItemDto entityToMovieItemDtoWithMovie(MovieItem movieItem);

    @IterableMapping(elementTargetType = MovieItemDto.class, qualifiedByName = "entityToMovieItemDtoWithMovie")
    List<MovieItemDto> entityToMovieItemDtoWithMovieList(List<MovieItem> movieItems);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieItemMetadataDto")
    MovieItemDto entityToMovieItemMetadataDto(MovieItem movieItem);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieRoomDto")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieItemRoomDto")
    MovieItemDto entityToMovieItemRoomDto(MovieItem movieItem);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieNotificationDto")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieItemNotificationDto")
    MovieItemNotificationDto entityToMovieItemNotificationDto(MovieItem movieItem);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "totalEpisode", target = "totalEpisode")
    @BeanMapping(ignoreByDefault = true)
    MovieItem fromCreateMovieItemFormToEntity(CreateMovieItemForm form);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "totalEpisode", target = "totalEpisode")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateMovieItemFormToEntity(UpdateMovieItemForm form, @MappingTarget MovieItem movieItem);
}
