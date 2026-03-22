package com.movie.api.mapper;

import com.movie.api.dto.collectionItem.CollectionItemDto;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.storage.model.CollectionItem;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MovieMapper.class})
public abstract class CollectionItemMapper {
    @Autowired
    protected MovieMapper movieMapper;

    @Mapping(source = "id", target = "id")
    @Mapping(source = "collection.id", target = "collectionId")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieAutoCompleteDto")
    @Mapping(source = "ordering", target = "ordering")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToCollectionItemDto")
    public abstract CollectionItemDto entityToCollectionItemDto(CollectionItem collectionItem);

    @IterableMapping(elementTargetType = CollectionItemDto.class, qualifiedByName = "entityToCollectionItemDto")
    public abstract List<CollectionItemDto> entityToCollectionItemDtoList(List<CollectionItem> collectionItems);

    @Named("collectionItemsToMovieDtos")
    public List<MovieDto> collectionItemsToMovieDtos(List<CollectionItem> collectionItems) {
        if (collectionItems == null || collectionItems.isEmpty()) {
            return new ArrayList<>();
        }
        return collectionItems.stream()
                .filter(item -> item.getMovie() != null) // Filter null movies
                .map(item -> movieMapper.entityToMovieAutoCompleteDto(item.getMovie()))
                .collect(Collectors.toList());
    }
}
