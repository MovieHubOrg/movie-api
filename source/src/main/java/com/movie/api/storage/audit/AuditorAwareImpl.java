package com.movie.api.storage.audit;

import com.movie.api.jwt.BaseJwt;
import com.movie.api.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    @Autowired
    private UserServiceImpl userService;

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("unknown");
        }

        BaseJwt tenantJwt = userService.getAddInfoFromToken() != null ? userService.getAddInfoFromToken() : null;
        if (tenantJwt == null || tenantJwt.getAccountId() == null) {
            return Optional.of("unknown");
        }
        return Optional.of(tenantJwt.getAccountId().toString());
    }
}
