package com.movie.api.mapper;

import com.movie.api.dto.notification.NotificationDto;
import com.movie.api.storage.model.Notification;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "body", target = "body")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "isRead", target = "isRead")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToNotificationDto")
    NotificationDto entityToNotificationDto(Notification notification);

    @IterableMapping(elementTargetType = NotificationDto.class, qualifiedByName = "entityToNotificationDto")
    List<NotificationDto> fromEntityToNotificationDtoList(List<Notification> notifications);
}
