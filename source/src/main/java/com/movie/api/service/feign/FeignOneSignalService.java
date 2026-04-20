package com.movie.api.service.feign;

import com.movie.api.cfg.CustomFeignConfig;
import com.movie.api.dto.oneSignal.OneSignalPushNotificationForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "file-one-signal-svr", url = "${onesignal.api-url}", configuration = CustomFeignConfig.class)
public interface FeignOneSignalService {
    @PostMapping(
            value = "/notifications?c=push",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<String> sendNotification(
            @RequestHeader("Authorization") String authorization,
            @RequestBody OneSignalPushNotificationForm form
    );
}
