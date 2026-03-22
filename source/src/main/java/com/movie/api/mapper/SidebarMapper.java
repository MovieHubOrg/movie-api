package com.movie.api.mapper;

import com.movie.api.dto.sidebar.SidebarDto;
import com.movie.api.form.sidebar.CreateSidebarForm;
import com.movie.api.form.sidebar.UpdateSidebarForm;
import com.movie.api.storage.model.Sidebar;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {MovieMapper.class})
public interface SidebarMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "movie", target = "movie", qualifiedByName = "entityToMovieDto")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "webThumbnailUrl", target = "webThumbnailUrl")
    @Mapping(source = "mobileThumbnailUrl", target = "mobileThumbnailUrl")
    @Mapping(source = "mainColor", target = "mainColor")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToSidebarDto")
    SidebarDto entityToSidebarDto(Sidebar sidebar);

    @IterableMapping(elementTargetType = SidebarDto.class, qualifiedByName = "entityToSidebarDto")
    List<SidebarDto> fromEntityToSidebarDtoList(List<Sidebar> Sidebars);

    @Mapping(source = "description", target = "description")
    @Mapping(source = "webThumbnailUrl", target = "webThumbnailUrl")
    @Mapping(source = "mobileThumbnailUrl", target = "mobileThumbnailUrl")
    @Mapping(source = "mainColor", target = "mainColor")
    @Mapping(source = "active", target = "active")
    @BeanMapping(ignoreByDefault = true)
    Sidebar fromCreateSidebarFormToEntity(CreateSidebarForm form);

    @Mapping(source = "description", target = "description")
    @Mapping(source = "webThumbnailUrl", target = "webThumbnailUrl")
    @Mapping(source = "mobileThumbnailUrl", target = "mobileThumbnailUrl")
    @Mapping(source = "mainColor", target = "mainColor")
    @Mapping(source = "active", target = "active")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateSidebarFormToEntity(UpdateSidebarForm form, @MappingTarget Sidebar sidebar);

}
