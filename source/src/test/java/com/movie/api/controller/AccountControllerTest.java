package com.movie.api.controller;

import com.movie.api.constant.BaseConstant;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.ResponseListDto;
import com.movie.api.dto.account.AccountDto;
import com.movie.api.mapper.AccountMapper;
import com.movie.api.storage.criteria.AccountCriteria;
import com.movie.api.storage.model.Account;
import com.movie.api.storage.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountController accountController;

    // ---------- listAccount ----------

    @Test
    void listAccount_forcesUserKindAndActiveStatus_returnsSuccessResponse() {
        AccountCriteria criteria = new AccountCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Account account = new Account();
        account.setId(1L);
        Page<Account> page = new PageImpl<>(Collections.singletonList(account));
        List<AccountDto> dtoList = Collections.singletonList(new AccountDto());

        when(accountRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(accountMapper.entityToAccountDtoAutoCompleteList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<AccountDto>>> response = accountController.listAccount(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEqualTo(dtoList);
        assertThat(response.getData().getTotalElements()).isEqualTo(1);
        assertThat(criteria.getKind()).isEqualTo(BaseConstant.ACCOUNT_KIND_USER);
        assertThat(criteria.getStatus()).isEqualTo(BaseConstant.STATUS_ACTIVE);
    }

    @Test
    void listAccount_whenEmptyResult_returnsEmptyList() {
        AccountCriteria criteria = new AccountCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(Collections.emptyList());

        when(accountRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(accountMapper.entityToAccountDtoAutoCompleteList(page.getContent())).thenReturn(Collections.emptyList());

        ApiMessageDto<ResponseListDto<List<AccountDto>>> response = accountController.listAccount(criteria, pageable);

        assertThat(response.getResult()).isTrue();
        assertThat(response.getData().getContent()).isEmpty();
        assertThat(response.getData().getTotalElements()).isEqualTo(0);
    }
}
