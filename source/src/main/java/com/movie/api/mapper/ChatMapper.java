package com.movie.api.mapper;

import com.movie.api.dto.chat.ChatDto;
import com.movie.api.storage.model.Chat;
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
public interface ChatMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "user", target = "user", qualifiedByName = "entityToAccountDto")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToChatDto")
    ChatDto entityToChatDto(Chat chat);

    @IterableMapping(elementTargetType = ChatDto.class, qualifiedByName = "entityToChatDto")
    List<ChatDto> fromEntityToChatDtoList(List<Chat> chats);
}
