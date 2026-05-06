package com.movie.api.mapper;

import com.movie.api.dto.movie.MovieDto;
import com.movie.api.dto.movie.MovieNotificationDto;
import com.movie.api.form.movie.CreateMovieForm;
import com.movie.api.form.movie.FilterMovieForm;
import com.movie.api.form.movie.UpdateMovieForm;
import com.movie.api.storage.criteria.MovieCriteria;
import com.movie.api.storage.model.Movie;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CategoryMapper.class})
public interface MovieMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "originalTitle", target = "originalTitle")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @Mapping(source = "imageTitleUrl", target = "imageTitleUrl")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "year", target = "year")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "isFeatured", target = "isFeatured")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "ageRating", target = "ageRating")
    @Mapping(source = "viewCount", target = "viewCount")
    @Mapping(source = "commentCount", target = "commentCount")
    @Mapping(source = "reviewCount", target = "reviewCount")
    @Mapping(source = "averageRating", target = "averageRating")
    @Mapping(source = "imdbId", target = "imdbId")
    @Mapping(source = "imdbRating", target = "imdbRating")
    @Mapping(source = "metadata", target = "metadata")
    @Mapping(source = "categories", target = "categories", qualifiedByName = "fromEntityToCategoryAutoCompleteDtoList")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieDto")
    MovieDto entityToMovieDto(Movie movie);

    @IterableMapping(elementTargetType = MovieDto.class, qualifiedByName = "entityToMovieDto")
    List<MovieDto> fromEntityToMovieDtoList(List<Movie> movies);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "originalTitle", target = "originalTitle")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @Mapping(source = "imageTitleUrl", target = "imageTitleUrl")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "year", target = "year")
    @Mapping(source = "isFeatured", target = "isFeatured")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "ageRating", target = "ageRating")
    @Mapping(source = "categories", target = "categories", qualifiedByName = "fromEntityToCategoryAutoCompleteDtoList")
    @Mapping(source = "viewCount", target = "viewCount")
    @Mapping(source = "commentCount", target = "commentCount")
    @Mapping(source = "reviewCount", target = "reviewCount")
    @Mapping(source = "averageRating", target = "averageRating")
    @Mapping(source = "imdbId", target = "imdbId")
    @Mapping(source = "imdbRating", target = "imdbRating")
    @Mapping(source = "metadata", target = "metadata")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieAutoCompleteDto")
    MovieDto entityToMovieAutoCompleteDto(Movie movie);

    @IterableMapping(elementTargetType = MovieDto.class, qualifiedByName = "entityToMovieAutoCompleteDto")
    List<MovieDto> fromEntityToMovieAutoCompleteDtoList(List<Movie> movies);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "originalTitle", target = "originalTitle")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "ageRating", target = "ageRating")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @Mapping(source = "imageTitleUrl", target = "imageTitleUrl")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "categories", target = "categories", qualifiedByName = "fromEntityToCategoryAutoCompleteDtoList")
    @Mapping(source = "imdbRating", target = "imdbRating")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieShortDto")
    MovieDto entityToMovieShortDto(Movie movie);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToMovieAutoCompleteShortDto")
    MovieDto fromEntityToMovieAutoCompleteShortDto(Movie movie);

    @IterableMapping(elementTargetType = MovieDto.class, qualifiedByName = "fromEntityToMovieAutoCompleteShortDto")
    List<MovieDto> fromEntityToMovieAutoCompleteShortDtoList(List<Movie> movies);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieSurveyDto")
    MovieDto entityToMovieSurveyDto(Movie movie);

    @IterableMapping(elementTargetType = MovieDto.class, qualifiedByName = "entityToMovieSurveyDto")
    List<MovieDto> fromEntityToMovieSurveyDtoList(List<Movie> movies);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "originalTitle", target = "originalTitle")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieNotificationDto")
    MovieNotificationDto entityToMovieNotificationDto(Movie movie);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "originalTitle", target = "originalTitle")
    @Mapping(source = "slug", target = "slug")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @Mapping(source = "type", target = "type")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToMovieRoomDto")
    MovieDto entityToMovieRoomDto(Movie movie);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "originalTitle", target = "originalTitle")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @Mapping(source = "imageTitleUrl", target = "imageTitleUrl")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "isFeatured", target = "isFeatured")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "ageRating", target = "ageRating")
    @Mapping(source = "imdbId", target = "imdbId")
    @Mapping(source = "year", target = "year")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    Movie fromCreateMovieFormToEntity(CreateMovieForm form);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "originalTitle", target = "originalTitle")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "posterUrl", target = "posterUrl")
    @Mapping(source = "imageTitleUrl", target = "imageTitleUrl")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "year", target = "year")
    @Mapping(source = "isFeatured", target = "isFeatured")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "ageRating", target = "ageRating")
    @Mapping(source = "imdbId", target = "imdbId")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateMovieFormToEntity(UpdateMovieForm form, @MappingTarget Movie movie);

    @Mapping(source = "type", target = "type")
    @Mapping(source = "isFeatured", target = "isFeatured")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "ageRating", target = "ageRating")
    @Mapping(source = "comingSoon", target = "comingSoon")
    @Mapping(source = "topImdb", target = "topImdb")
    @Mapping(source = "categoryIds", target = "categoryIds")
    @BeanMapping(ignoreByDefault = true)
    MovieCriteria fromFilterMovieFromToMovieCriteria(FilterMovieForm form);
}
