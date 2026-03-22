package com.movie.api.service.feign;

import com.movie.api.cfg.CustomFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-svr", url = "${auth.internal.base.url}", configuration = CustomFeignConfig.class)
public interface FeignAccountAuthService {
    public static final String LOGIN_TYPE = "BASIC_LOGIN_AUTH";

    @PostMapping(value = "/api/token")
    OAuth2AccessToken authLogin(@RequestHeader(LOGIN_TYPE) String type, @RequestParam MultiValueMap<String, String> request);
}
