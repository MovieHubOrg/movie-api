package com.movie.api.dto.moviePerson;

import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.dto.person.PersonDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class MoviePersonDto extends ABasicAdminDto {
    private MovieDto movie;
    private PersonDto person;
    private Integer kind;
    private String characterName;
    private Integer ordering;
}
