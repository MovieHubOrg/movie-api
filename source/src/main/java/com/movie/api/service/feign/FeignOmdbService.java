package com.movie.api.service.feign;

import com.movie.api.cfg.CustomFeignConfig;
import com.movie.api.cfg.component.OAuth2FeignRequestInterceptor;
import com.movie.api.dto.movie.OmdbRatingResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "omdb-svr", url = "${omdb.api.url}", configuration = CustomFeignConfig.class)
public interface FeignOmdbService {
    @GetMapping("/")
    OmdbRatingResponseDto getByImdbId(@RequestHeader(OAuth2FeignRequestInterceptor.HEADER_BYPASS) String bypass,
                                      @RequestParam("apikey") String apiKey,
                                      @RequestParam("i") String imdbId);
}
