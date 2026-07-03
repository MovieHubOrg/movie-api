package com.movie.api.mapper;

import com.movie.api.dto.room.RoomDto;
import com.movie.api.dto.room.RoomNotificationDto;
import com.movie.api.form.room.CreateRoomForm;
import com.movie.api.storage.model.Room;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MovieItemMapper.class, AccountMapper.class})
public interface RoomMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "movieItem", target = "movieItem", qualifiedByName = "entityToMovieItemRoomDto")
    @Mapping(source = "host", target = "host", qualifiedByName = "entityToAccountDto")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "participantCount", target = "participantCount")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "reasonEnd", target = "reasonEnd")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToRoomDto")
    RoomDto entityToRoomDto(Room room);

    @IterableMapping(elementTargetType = RoomDto.class, qualifiedByName = "entityToRoomDto")
    List<RoomDto> fromEntityToRoomDtoList(List<Room> rooms);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "movieItem.id", target = "movieItemId")
    @Mapping(source = "movieItem.movie.id", target = "movieId")
    @Mapping(source = "movieItem.movie.title", target = "movieTitle")
    @Mapping(source = "movieItem.thumbnailUrl", target = "movieThumbnail")
    @Mapping(source = "host", target = "host", qualifiedByName = "entityToAccountNotificationDto")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToRoomNotificationDto")
    RoomNotificationDto entityToRoomNotificationDto(Room room);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "kind", target = "kind")
    @BeanMapping(ignoreByDefault = true)
    Room fromCreateRoomFormToEntity(CreateRoomForm form);
}
