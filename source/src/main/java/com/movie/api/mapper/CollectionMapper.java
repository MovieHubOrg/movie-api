package com.movie.api.mapper;

import com.movie.api.dto.collection.CollectionDto;
import com.movie.api.form.collection.CreateCollectionForm;
import com.movie.api.form.collection.UpdateCollectionForm;
import com.movie.api.storage.model.Collection;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CollectionItemMapper.class, StyleMapper.class})
public interface CollectionMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "color", target = "color")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "style", target = "style", qualifiedByName = "entityToStyleDto")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "filter", target = "filter")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToCollectionDto")
    CollectionDto entityToCollectionDto(Collection collection);

    @IterableMapping(elementTargetType = CollectionDto.class, qualifiedByName = "entityToCollectionDto")
    List<CollectionDto> entityToCollectionDtoList(List<Collection> collections);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "filter", target = "filter")
    @BeanMapping(ignoreByDefault = true)
    Collection fromCreateCollectionFormToEntity(CreateCollectionForm form);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "filter", target = "filter")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateCollectionFormToEntity(UpdateCollectionForm form, @MappingTarget Collection collection);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "color", target = "color")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "style.type", target = "styleType")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "filter", target = "filter")
    @Mapping(source = "collectionItems", target = "movies", qualifiedByName = "collectionItemsToMovieDtos")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToCollectionDetailsDto")
    CollectionDto entityToCollectionDetailsDto(Collection collection);

    @IterableMapping(elementTargetType = CollectionDto.class, qualifiedByName = "entityToCollectionDetailsDto")
    List<CollectionDto> entityToCollectionDetailsDtoList(List<Collection> collections);
}
