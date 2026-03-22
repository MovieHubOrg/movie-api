package com.movie.api.service.feign;

import com.movie.api.cfg.CustomFeignConfig;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.form.file.DeleteListFileForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "file-media-svr", url = "${media.internal.base.url}", configuration = CustomFeignConfig.class)
public interface FeignFileMediaService {
    @PostMapping(value = "/v1/file/delete-list-file")
    ApiMessageDto<String> deleteListFile(@RequestHeader(FeignConstant.HEADER_AUTHORIZATION) String bearerToken, @RequestBody DeleteListFileForm deleteListFileForm);
}
