package com.movie.api.mapper;

import com.movie.api.dto.participant.ParticipantDto;
import com.movie.api.storage.model.Participant;
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
        uses = {AccountMapper.class, RoomMapper.class})
public interface ParticipantMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "user", target = "user", qualifiedByName = "entityToAccountDto")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "room", target = "room", qualifiedByName = "entityToRoomDto")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToParticipantDto")
    ParticipantDto entityToParticipantDto(Participant participant);

    @IterableMapping(elementTargetType = ParticipantDto.class, qualifiedByName = "entityToParticipantDto")
    List<ParticipantDto> fromEntityToParticipantDtoList(List<Participant> participants);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "user", target = "user", qualifiedByName = "entityToAccountDto")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "state", target = "state")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToParticipantDtoForRoom")
    ParticipantDto entityToParticipantDtoForRoom(Participant participant);

    @IterableMapping(elementTargetType = ParticipantDto.class, qualifiedByName = "entityToParticipantDtoForRoom")
    List<ParticipantDto> fromEntityToParticipantDtoForRoomList(List<Participant> participants);
}
