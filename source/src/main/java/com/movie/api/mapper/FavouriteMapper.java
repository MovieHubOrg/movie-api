package com.movie.api.mapper;

import com.movie.api.dto.favourite.FavouriteDto;
import com.movie.api.storage.model.Favourite;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MovieMapper.class, PersonMapper.class})
public interface FavouriteMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieAutoCompleteDto")
    @Mapping(source = "person", target = "person", qualifiedByName = "entityToPersonAutoCompleteDto")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToFavouriteDto")
    FavouriteDto entityToFavouriteDto(Favourite favourite);

    @IterableMapping(elementTargetType = FavouriteDto.class, qualifiedByName = "entityToFavouriteDto")
    List<FavouriteDto> fromEntityToFavouriteDtoList(List<Favourite> favourites);
}
