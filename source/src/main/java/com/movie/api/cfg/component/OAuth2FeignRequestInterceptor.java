package com.movie.api.cfg.component;

import com.movie.api.service.feign.FeignAccountAuthService;
import com.movie.api.service.feign.FeignConstant;
import com.movie.api.service.impl.UserServiceImpl;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
public class OAuth2FeignRequestInterceptor implements RequestInterceptor { // cấu hình cho các cuộc gọi đến các service khác

    private static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String HEADER_X_API_KEY = "X-Api-Key";
    public static final String HEADER_BYPASS = "Bypass";
    private static final String BEARER_TOKEN_TYPE = "Bearer";
    private static final String BASIC_AUTH_TYPE = "Basic";

    @Value("${auth.internal.basic.username}")
    private String internalAuthUsername;
    @Value("${auth.internal.basic.password}")
    private String internalAuthpassword;

    @Autowired
    private UserServiceImpl userService;

    @Override
    public void apply(RequestTemplate template) {
        if (template.headers().containsKey(FeignAccountAuthService.LOGIN_TYPE)) {
            if (Objects.equals(template.headers().get(FeignAccountAuthService.LOGIN_TYPE).toArray()[0], FeignConstant.LOGIN_TYPE_INTERNAL)) {
                String auth = internalAuthUsername + ":" + internalAuthpassword;
                log.error("-----------> internal = " + auth);
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
                template.header(AUTHORIZATION_HEADER, String.format("%s %s", BASIC_AUTH_TYPE, new String(encodedAuth)));
            } else {
                log.error("-----------> not found type = " + template.headers().get(FeignAccountAuthService.LOGIN_TYPE).toArray()[0]);
            }
            template.removeHeader(FeignAccountAuthService.LOGIN_TYPE);
        } else if (template.headers().containsKey(HEADER_BYPASS)) {
            template.removeHeader(HEADER_BYPASS);
        }else {
            if (!template.headers().containsKey(AUTHORIZATION_HEADER) && !template.headers().containsKey(HEADER_X_API_KEY) && !template.headers().containsKey(HEADER_X_API_KEY)) {
                log.error("-----------> Constructing Header {} for Token {}, token {}", AUTHORIZATION_HEADER, BEARER_TOKEN_TYPE, String.format("%s %s", BEARER_TOKEN_TYPE, userService.AUTH_SERVER_TOKEN));
                template.header(AUTHORIZATION_HEADER, String.format("%s %s", BEARER_TOKEN_TYPE, userService.getToken()));
            }
        }
    }
}
