package com.movie.api.scheduler;

import com.movie.api.service.RecommendationDataService;
import com.movie.api.service.RecommendationPythonClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RecommendationScheduler {
    @Autowired
    private RecommendationDataService recommendationDataService;

    @Autowired
    private RecommendationPythonClient recommendationPythonClient;

    @Value("${recommendation.rebuild.enabled:true}")
    private Boolean recommendationRebuildEnabled;

    @Scheduled(cron = "${recommendation.rebuild.cron:0 0 3 * * *}", zone = "UTC")
    public void rebuildRecommendationData() {
        if (!Boolean.TRUE.equals(recommendationRebuildEnabled)) {
            return;
        }

        try {
            log.warn("======> Start scheduler rebuildRecommendationData");
            recommendationDataService.rebuildUserMovieData();
            recommendationDataService.rebuildUserMovieScores();
            recommendationPythonClient.triggerItemKnnRebuild();
            recommendationPythonClient.triggerHybridRebuild();
            log.warn("======> End scheduler rebuildRecommendationData");
        } catch (Exception ex) {
            log.error("Failed to rebuild recommendation data", ex);
        }
    }
}
