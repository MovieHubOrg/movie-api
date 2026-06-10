package com.movie.api.service;

import com.movie.api.cfg.component.OAuth2FeignRequestInterceptor;
import com.movie.api.service.feign.FeignRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecommendationPythonClient {
    @Autowired
    private FeignRecommendationService feignRecommendationService;

    public void triggerItemKnnRebuild() {
        try {
            feignRecommendationService.rebuildItemKnn(OAuth2FeignRequestInterceptor.HEADER_BYPASS);
            log.warn("Triggered Python item-KNN rebuild");
        } catch (Exception ex) {
            log.error("Failed to trigger Python item-KNN rebuild", ex);
        }
    }

    public void triggerUserKnnRebuild() {
        try {
            feignRecommendationService.rebuildUserKnn(OAuth2FeignRequestInterceptor.HEADER_BYPASS);
            log.warn("Triggered Python user-KNN rebuild");
        } catch (Exception ex) {
            log.error("Failed to trigger Python user-KNN rebuild", ex);
        }
    }

    public void triggerHybridRebuild() {
        try {
            feignRecommendationService.rebuildHybrid(OAuth2FeignRequestInterceptor.HEADER_BYPASS);
            log.warn("Triggered Python hybrid rebuild");
        } catch (Exception ex) {
            log.error("Failed to trigger Python hybrid rebuild", ex);
        }
    }
}
