package com.movie.api.mapper;

import com.movie.api.dto.person.PersonDto;
import com.movie.api.form.person.CreatePersonForm;
import com.movie.api.form.person.UpdatePersonForm;
import com.movie.api.storage.model.Person;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface PersonMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "otherName", target = "otherName")
    @Mapping(source = "avatarPath", target = "avatarPath")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "kinds", target = "kinds")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToPersonDto")
    PersonDto entityToPersonDto(Person person);

    @IterableMapping(elementTargetType = PersonDto.class, qualifiedByName = "entityToPersonDto")
    List<PersonDto> fromEntityToPersonDtoList(List<Person> persons);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "otherName", target = "otherName")
    @Mapping(source = "avatarPath", target = "avatarPath")
    @Mapping(source = "kinds", target = "kinds")
    @Mapping(source = "country", target = "country")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToPersonAutoCompleteDto")
    PersonDto entityToPersonAutoCompleteDto(Person person);

    @IterableMapping(elementTargetType = PersonDto.class, qualifiedByName = "entityToPersonAutoCompleteDto")
    List<PersonDto> fromEntityToPersonAutoCompleteDtoList(List<Person> persons);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "otherName", target = "otherName")
    @Mapping(source = "avatarPath", target = "avatarPath")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "kinds", target = "kinds")
    @BeanMapping(ignoreByDefault = true)
    Person fromCreatePersonFormToEntity(CreatePersonForm form);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "otherName", target = "otherName")
    @Mapping(source = "avatarPath", target = "avatarPath")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "kinds", target = "kinds")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdatePersonFormToEntity(UpdatePersonForm form, @MappingTarget Person person);
}
