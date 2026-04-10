package com.movie.api.service;

import com.movie.api.constant.BaseConstant;
import com.movie.api.form.user.AccountEventForm;
import com.movie.api.storage.model.Account;
import com.movie.api.storage.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountSyncService {
    private final AccountRepository accountRepository;

    public void syncCreated(AccountEventForm event) {
        Account account = new Account();
        updateFields(account, event);
        accountRepository.save(account);
        log.info("Synced CREATED accountId={}", event.getId());
    }

    public void syncUpdated(AccountEventForm event) {
        Account account = accountRepository
                .findById(event.getId())
                .orElse(new Account());
        updateFields(account, event);
        accountRepository.save(account);
        log.info("Synced UPDATED accountId={}", event.getId());
    }

    public void syncDeleted(AccountEventForm event) {
        accountRepository.findById(event.getId()).ifPresent(account -> {
            account.setStatus(BaseConstant.STATUS_DELETE); // soft delete
            accountRepository.save(account);
            log.info("Synced DELETED accountId={}", event.getId());
        });
    }

    public void syncStatusChanged(AccountEventForm event) {
        accountRepository.findById(event.getId()).ifPresent(account -> {
            account.setStatus(event.getStatus());
            accountRepository.save(account);
            log.info("Synced STATUS_CHANGED accountId={}, status={}", event.getId(), event.getStatus());
        });
    }

    private void updateFields(Account account, AccountEventForm event) {
        account.setId(event.getId());
        account.setKind(event.getKind());
        account.setUsername(event.getUsername());
        account.setPhone(event.getPhone());
        account.setEmail(event.getEmail());
        account.setFullName(event.getFullName());
        account.setAvatarPath(event.getAvatarPath());
        account.setGender(event.getGender());
        account.setStatus(event.getStatus());
    }
}
