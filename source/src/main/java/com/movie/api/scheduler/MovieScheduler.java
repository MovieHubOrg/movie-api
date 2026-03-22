package com.movie.api.scheduler;

import com.movie.api.storage.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MovieScheduler {
    @Autowired
    private MovieRepository movieRepository;

    @Scheduled(cron = "0 0 */2 * * *", zone = "UTC")
    public void updateViewCount() {
        log.warn("======> Start scheduler updateViewCount movie");
        movieRepository.updateViewCount();
        log.warn("======> End scheduler updateViewCount movie");
    }
}
