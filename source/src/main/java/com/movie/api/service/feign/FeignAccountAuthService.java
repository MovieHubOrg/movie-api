package com.movie.api.service.feign;

import com.movie.api.cfg.CustomFeignConfig;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.form.file.DeleteListFileForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-svr", url = "${auth.internal.base.url}", configuration = CustomFeignConfig.class)
public interface FeignAccountAuthService {
    public static final String LOGIN_TYPE = "BASIC_LOGIN_AUTH";

    @PostMapping(value = "/api/token")
    OAuth2AccessToken authLogin(@RequestHeader(LOGIN_TYPE) String type, @RequestParam MultiValueMap<String, String> request);

    @PutMapping(value = "/v1/user/update-make-survey")
    ApiMessageDto<Void> updateMakeSurvey(@RequestHeader(FeignConstant.HEADER_AUTHORIZATION) String bearerToken);
}
