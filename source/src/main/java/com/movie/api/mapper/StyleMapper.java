package com.movie.api.mapper;

import com.movie.api.dto.style.StyleDto;
import com.movie.api.form.style.CreateStyleForm;
import com.movie.api.form.style.UpdateStyleForm;
import com.movie.api.storage.model.Style;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StyleMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "imageMobileUrl", target = "imageMobileUrl")
    @Mapping(source = "imageWebUrl", target = "imageWebUrl")
    @Mapping(source = "isDefault", target = "isDefault")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToStyleDto")
    StyleDto entityToStyleDto(Style style);

    @IterableMapping(elementTargetType = StyleDto.class, qualifiedByName = "entityToStyleDto")
    List<StyleDto> entityToStyleDtoList(List<Style> styles);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "isDefault", target = "isDefault")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToStyleAutoCompleteDto")
    StyleDto entityToStyleAutoCompleteDto(Style style);

    @IterableMapping(elementTargetType = StyleDto.class, qualifiedByName = "entityToStyleAutoCompleteDto")
    List<StyleDto> entityToStyleAutoCompleteDtoList(List<Style> styles);

    @Mapping(source = "type", target = "type")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "imageMobileUrl", target = "imageMobileUrl")
    @Mapping(source = "imageWebUrl", target = "imageWebUrl")
    @Mapping(source = "isDefault", target = "isDefault")
    @BeanMapping(ignoreByDefault = true)
    Style fromCreateStyleFormToEntity(CreateStyleForm form);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "imageMobileUrl", target = "imageMobileUrl")
    @Mapping(source = "imageWebUrl", target = "imageWebUrl")
    @Mapping(source = "isDefault", target = "isDefault")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateStyleFormToEntity(UpdateStyleForm form, @MappingTarget Style style);
}
