package com.movie.api.scheduler;

import com.movie.api.service.ImdbService;
import com.movie.api.storage.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MovieScheduler {
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ImdbService imdbService;

    @Value("${imdb.ratings.enabled}")
    private Boolean imdbRatingsEnabled;

    @Scheduled(cron = "0 0 */2 * * *", zone = "UTC")
    public void updateViewCount() {
        log.warn("======> Start scheduler updateViewCount movie");
        movieRepository.updateViewCount();
        log.warn("======> End scheduler updateViewCount movie");
    }

    @Scheduled(cron = "0 30 2 * * *", zone = "UTC")
    public void updateImdbRatings() {
        if (!Boolean.TRUE.equals(imdbRatingsEnabled)) {
            return;
        }

        try {
            log.warn("======> Start scheduler updateImdbRatings movie");
            imdbService.syncAllMovieRatings();
            log.warn("======> End scheduler updateImdbRatings movie");
        } catch (Exception ex) {
            log.error("Failed to update IMDb ratings", ex);
        }
    }
}
