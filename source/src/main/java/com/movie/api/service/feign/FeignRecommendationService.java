package com.movie.api.service.feign;

import com.movie.api.cfg.CustomFeignConfig;
import com.movie.api.cfg.component.OAuth2FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "recommendation-svr", url = "${recommendation.python.base-url}", configuration = CustomFeignConfig.class)
public interface FeignRecommendationService {
    @PostMapping(value = "/v1/recommendation-jobs/item-knn/rebuild")
    void rebuildItemKnn(@RequestHeader(OAuth2FeignRequestInterceptor.HEADER_BYPASS) String bypass);

    @PostMapping(value = "/v1/recommendation-jobs/user-knn/rebuild")
    void rebuildUserKnn(@RequestHeader(OAuth2FeignRequestInterceptor.HEADER_BYPASS) String bypass);

    @PostMapping(value = "/v1/recommendation-jobs/hybrid/rebuild")
    void rebuildHybrid(@RequestHeader(OAuth2FeignRequestInterceptor.HEADER_BYPASS) String bypass);
}
