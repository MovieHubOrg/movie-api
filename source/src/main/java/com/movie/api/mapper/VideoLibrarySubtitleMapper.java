package com.movie.api.mapper;

import com.movie.api.dto.video.VideoLibrarySubtitleDto;
import com.movie.api.dto.video.VideoLibrarySubtitleNotificationDto;
import com.movie.api.form.video.UpdateVideoLibrarySubtitleForm;
import com.movie.api.storage.model.VideoLibrarySubtitle;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VideoLibrarySubtitleMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "videoLibrary.id", target = "videoLibraryId")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "fileUrl", target = "fileUrl")
    @Mapping(source = "isDefault", target = "isDefault")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToVideoLibrarySubtitleDto")
    VideoLibrarySubtitleDto entityToVideoLibrarySubtitleDto(VideoLibrarySubtitle entity);

    @IterableMapping(elementTargetType = VideoLibrarySubtitleDto.class, qualifiedByName = "entityToVideoLibrarySubtitleDto")
    List<VideoLibrarySubtitleDto> fromEntityToVideoLibrarySubtitleDtoList(List<VideoLibrarySubtitle> entities);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "videoLibrary.id", target = "videoLibraryId")
    @Mapping(source = "videoLibrary.thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "videoLibrary.sourceType", target = "sourceType")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "state", target = "state")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToVideoLibrarySubtitleNotificationDto")
    VideoLibrarySubtitleNotificationDto entityToVideoLibrarySubtitleNotificationDto(VideoLibrarySubtitle entity);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "label", target = "label")
    @Mapping(source = "fileUrl", target = "fileUrl")
    @Mapping(source = "isDefault", target = "isDefault")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToVideoLibrarySubtitlePublicDto")
    VideoLibrarySubtitleDto entityToVideoLibrarySubtitlePublicDto(VideoLibrarySubtitle entity);

    @IterableMapping(elementTargetType = VideoLibrarySubtitleDto.class, qualifiedByName = "entityToVideoLibrarySubtitlePublicDto")
    List<VideoLibrarySubtitleDto> entityToVideoLibrarySubtitlePublicDtoList(List<VideoLibrarySubtitle> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdateFormToEntity(UpdateVideoLibrarySubtitleForm form, @MappingTarget VideoLibrarySubtitle entity);
}
