package com.movie.api.dto.room;

import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.account.AccountDto;
import com.movie.api.dto.movieItem.MovieItemDto;
import com.movie.api.dto.participant.ParticipantDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ApiModel
public class RoomDto extends ABasicAdminDto {
    private String name;
    private String code;
    private Integer kind;
    private MovieItemDto movieItem;
    private AccountDto host;
    private Date startTime;
    private Date endTime;
    private Integer state;
    private Integer participantCount;
    private Integer currentViewers;
    private String reasonEnd;
    private List<ParticipantDto> participants;
}
