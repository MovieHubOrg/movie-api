package com.movie.api.dto.participant;

import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.account.AccountDto;
import com.movie.api.dto.room.RoomDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ParticipantDto extends ABasicAdminDto {
    private AccountDto user;
    private Integer role;
    private RoomDto room;
    private Integer state;
}
