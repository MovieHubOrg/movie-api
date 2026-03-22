package com.movie.api.scheduler;

import com.movie.api.storage.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserScheduler {
    @Autowired
    private AccountRepository accountRepository;

//    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
//    public void deleteUserPendingBefore1Days() {
//        log.warn("======> Start scheduler deleteUserPendingBefore1Days user");
//        Date date = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
//        accountRepository.deleteUserPendingBeforeDate(date);
//        log.warn("======> End scheduler deleteUserPendingBefore1Days user");
//    }
}
