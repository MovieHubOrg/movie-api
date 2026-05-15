package com.movie.api.mapper;

import com.movie.api.dto.video.VideoLibraryDto;
import com.movie.api.dto.video.VideoLibraryNotificationDto;
import com.movie.api.form.video.CreateVideoLibraryForm;
import com.movie.api.form.video.UpdateVideoForm;
import com.movie.api.form.video.UpdateVideoLibraryForm;
import com.movie.api.storage.model.VideoLibrary;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface VideoLibraryMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "sourceType", target = "sourceType")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "relativeContentPath", target = "relativeContentPath")
    @Mapping(source = "spriteUrl", target = "spriteUrl")
    @Mapping(source = "vttUrl", target = "vttUrl")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "introStart", target = "introStart")
    @Mapping(source = "introEnd", target = "introEnd")
    @Mapping(source = "outroStart", target = "outroStart")
    @Mapping(source = "duration", target = "duration")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "audioState", target = "audioState")
    @Mapping(source = "audioUrl", target = "audioUrl")
    @Mapping(source = "reason", target = "reason")
    @Mapping(source = "serverConfig.hostname", target = "hostname")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToVideoLibraryDto")
    VideoLibraryDto entityToVideoLibraryDto(VideoLibrary video);

    @IterableMapping(elementTargetType = VideoLibraryDto.class, qualifiedByName = "entityToVideoLibraryDto")
    List<VideoLibraryDto> fromEntityToVideoLibraryDtoList(List<VideoLibrary> videoLibraries);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToVideoLibraryAutoCompleteDto")
    VideoLibraryDto entityToVideoLibraryAutoCompleteDto(VideoLibrary video);

    @IterableMapping(elementTargetType = VideoLibraryDto.class, qualifiedByName = "entityToVideoLibraryAutoCompleteDto")
    List<VideoLibraryDto> fromEntityToVideoLibraryAutoCompleteDtoList(List<VideoLibrary> videoLibraries);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "sourceType", target = "sourceType")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "relativeContentPath", target = "relativeContentPath")
    @Mapping(source = "spriteUrl", target = "spriteUrl")
    @Mapping(source = "vttUrl", target = "vttUrl")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "duration", target = "duration")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "audioState", target = "audioState")
    @Mapping(source = "audioUrl", target = "audioUrl")
    @Mapping(source = "introStart", target = "introStart")
    @Mapping(source = "introEnd", target = "introEnd")
    @Mapping(source = "outroStart", target = "outroStart")
    @Mapping(source = "serverConfig.hostname", target = "hostname")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToVideoLibraryShortDto")
    VideoLibraryDto entityToVideoLibraryShortDto(VideoLibrary video);

    @IterableMapping(elementTargetType = VideoLibraryDto.class, qualifiedByName = "entityToVideoLibraryShortDto")
    List<VideoLibraryDto> fromEntityToVideoLibraryShortDtoList(List<VideoLibrary> videoLibraries);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "duration", target = "duration")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "reason", target = "reason")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToVideoLibraryDtoNotification")
    VideoLibraryNotificationDto entityToVideoLibraryDtoNotification(VideoLibrary video);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "sourceType", target = "sourceType")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "introStart", target = "introStart")
    @Mapping(source = "introEnd", target = "introEnd")
    @Mapping(source = "outroStart", target = "outroStart")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    VideoLibrary fromCreateVideoLibraryFormToEntity(CreateVideoLibraryForm form);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "thumbnailUrl", target = "thumbnailUrl")
    @Mapping(source = "introStart", target = "introStart")
    @Mapping(source = "introEnd", target = "introEnd")
    @Mapping(source = "outroStart", target = "outroStart")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateVideoLibraryFormToEntity(UpdateVideoLibraryForm form, @MappingTarget VideoLibrary videoLibrary);

    @Mapping(source = "content", target = "content")
    @Mapping(source = "relativeContentPath", target = "relativeContentPath")
    @Mapping(source = "spriteUrl", target = "spriteUrl")
    @Mapping(source = "vttUrl", target = "vttUrl")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "duration", target = "duration")
    @Mapping(source = "reason", target = "reason")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateVideoFormToEntity(UpdateVideoForm form, @MappingTarget VideoLibrary videoLibrary);
}
