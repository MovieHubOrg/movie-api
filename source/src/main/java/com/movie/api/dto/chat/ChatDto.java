package com.movie.api.dto.chat;

import com.movie.api.dto.ABasicAdminDto;
import com.movie.api.dto.account.AccountDto;
import com.movie.api.dto.room.RoomDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ChatDto extends ABasicAdminDto {
    private AccountDto user;
    private RoomDto room;
    private String content;
}
