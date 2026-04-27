package com.movie.api.storage.repository;

import com.movie.api.storage.model.Account;
import io.swagger.models.auth.In;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByIdAndStatusAndKind(Long id, Integer status, Integer kind);

    List<Account> findAllByStatusAndKindIn(Integer status, List<Integer> kind);

    List<Account> findAllByIdInAndStatus(List<Long> ids, Integer status);

    List<Account> findAllByIdInAndKindAndStatus(List<Long> ids, Integer kind, Integer status);
}
