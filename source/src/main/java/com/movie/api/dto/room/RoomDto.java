package com.movie.api.dto.room;

import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.account.AccountDto;
import com.movie.api.dto.movieItem.MovieItemDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@ApiModel
public class RoomDto extends ABasicAdminDto {
    private String code;
    private Integer kind;
    private MovieItemDto movieItem;
    private AccountDto host;
    private Date startTime;
    private Date endTime;
    private Integer state;
    private Integer participantCount;
}
