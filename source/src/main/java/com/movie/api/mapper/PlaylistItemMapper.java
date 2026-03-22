package com.movie.api.mapper;

import com.movie.api.dto.movie.MovieDto;
import com.movie.api.storage.model.PlaylistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MovieMapper.class})
public abstract class PlaylistItemMapper {
    @Autowired
    protected MovieMapper movieMapper;

    @Named("playlistItemsToMovieDtos")
    public List<MovieDto> playlistItemsToMovieDtos(List<PlaylistItem> playlistItems) {
        if (playlistItems == null || playlistItems.isEmpty()) {
            return new ArrayList<>();
        }

        return playlistItems.stream()
                .filter(item -> item.getMovie() != null) // Filter null movies
                .map(item -> movieMapper.entityToMovieAutoCompleteDto(item.getMovie()))
                .collect(Collectors.toList());
    }
}
