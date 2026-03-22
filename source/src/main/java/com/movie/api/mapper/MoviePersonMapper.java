package com.movie.api.mapper;

import com.movie.api.dto.moviePerson.MoviePersonDto;
import com.movie.api.form.moviePerson.CreateMoviePersonForm;
import com.movie.api.storage.model.MoviePerson;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MovieMapper.class, PersonMapper.class})
public interface MoviePersonMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieAutoCompleteDto")
    @Mapping(source = "person", target = "person", qualifiedByName = "entityToPersonAutoCompleteDto")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "characterName", target = "characterName")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMoviePersonDto")
    MoviePersonDto entityToMoviePersonDto(MoviePerson moviePerson);

    @IterableMapping(elementTargetType = MoviePersonDto.class, qualifiedByName = "entityToMoviePersonDto")
    List<MoviePersonDto> fromEntityToMoviePersonDtoList(List<MoviePerson> moviePersons);

    @Mapping(source = "kind", target = "kind")
    @BeanMapping(ignoreByDefault = true)
    MoviePerson fromCreateMoviePersonFormToEntity(CreateMoviePersonForm form);

}
