package com.movie.api.mapper;

import com.movie.api.dto.userReport.UserReportDto;
import com.movie.api.dto.userReport.UserReportMetadataDto;
import com.movie.api.dto.userReport.UserReportNotificationDto;
import com.movie.api.form.CreateUserReportForm;
import com.movie.api.form.video.UpdateVideoLibraryForm;
import com.movie.api.storage.model.UserReport;
import com.movie.api.storage.model.VideoLibrary;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {AccountMapper.class})
public interface UserReportMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "user", target = "user", qualifiedByName = "entityToAccountDto")
    @Mapping(source = "objectId", target = "objectId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToDto")
    UserReportDto entityToDto(UserReport userReport);

    @IterableMapping(elementTargetType = UserReportDto.class, qualifiedByName = "entityToDto")
    List<UserReportDto> entityToDtoList(List<UserReport> userReports);

    @Mapping(source = "type", target = "type")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "objectId", target = "objectId")
    @BeanMapping(ignoreByDefault = true)
    UserReport fromCreateFormToEntity(CreateUserReportForm form);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "user", target = "user", qualifiedByName = "entityToAccountNotificationDto")
    @Mapping(source = "objectId", target = "objectId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToNotificationDto")
    UserReportNotificationDto entityToNotificationDto(UserReport userReport);

    @Mapping(source = "movieItemId", target = "movieItemId")
    @Mapping(source = "movieId", target = "movieId")
    @Mapping(source = "movieTitle", target = "movieTitle")
    @Mapping(source = "movieThumbnail", target = "movieThumbnail")
    @Mapping(source = "parentId", target = "parentId")
    @BeanMapping(ignoreByDefault = true)
    void updateFromMetaDataToUserReportNotificationDto(UserReportMetadataDto metadata, @MappingTarget UserReportNotificationDto data);
}
