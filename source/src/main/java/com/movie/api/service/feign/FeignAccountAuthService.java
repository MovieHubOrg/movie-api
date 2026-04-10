package com.movie.api.service.feign;

import com.movie.api.cfg.CustomFeignConfig;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.form.user.UpdateMakeSurveyForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-svr", url = "${auth.internal.base.url}", configuration = CustomFeignConfig.class)
public interface FeignAccountAuthService {
    String LOGIN_TYPE = "BASIC_LOGIN_AUTH";

    @PostMapping(value = "/api/token")
    OAuth2AccessToken authLogin(@RequestHeader(LOGIN_TYPE) String type, @RequestParam MultiValueMap<String, String> request);

    @PutMapping(value = "/v1/user/internal/update-make-survey", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiMessageDto<Void> updateMakeSurvey(@RequestHeader(FeignConstant.HEADER_X_API_KEY) String apiKey,
                                         @RequestBody UpdateMakeSurveyForm form);
}
