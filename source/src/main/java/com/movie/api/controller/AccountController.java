package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.account.AccountDto;
import com.movie.api.mapper.AccountMapper;
import com.movie.api.storage.criteria.AccountCriteria;
import com.movie.api.storage.model.Account;
import com.movie.api.storage.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/account")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AccountController extends ABasicController {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountMapper accountMapper;

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<AccountDto>>> listAccount(AccountCriteria accountCriteria, Pageable pageable) {
        accountCriteria.setKind(BaseConstant.ACCOUNT_KIND_USER);
        accountCriteria.setStatus(BaseConstant.STATUS_ACTIVE);
        Page<Account> accounts = accountRepository.findAll(accountCriteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(accounts, accountMapper::entityToAccountDtoAutoCompleteList), "List account success");
    }
}
