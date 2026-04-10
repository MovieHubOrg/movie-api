package com.movie.api.dto.favourite;

import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.account.AccountDto;
import com.movie.api.dto.movie.MovieDto;
import com.movie.api.dto.person.PersonDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class FavouriteDto extends ABasicAdminDto {
    private AccountDto user;
    private Integer type;
    private MovieDto movie;
    private PersonDto person;
}
