package com.movie.api.mapper;

import com.movie.api.dto.account.AccountDto;
import com.movie.api.dto.account.AccountNotificationDto;
import com.movie.api.storage.model.Account;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "avatarPath", target = "avatarPath")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "isVip", target = "isVip")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToAccountDto")
    AccountDto entityToAccountDto(Account account);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "avatarPath", target = "avatarPath")
    @BeanMapping(ignoreByDefault = true)
    @Named("entityToAccountNotificationDto")
    AccountNotificationDto entityToAccountNotificationDto(Account account);
}
