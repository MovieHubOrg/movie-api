package com.movie.api.service.feign;

import com.movie.api.cfg.CustomFeignConfig;
import com.movie.api.dto.ApiMessageDto;
import com.movie.api.dto.account.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-svr", url = "${auth.internal.base.url}", configuration = CustomFeignConfig.class)
public interface FeignCustomerAuthService {
    @GetMapping(value = "/v1/customer/get/{id}")
    ApiMessageDto<CustomerDto> get(@PathVariable("id") Long id);
}
