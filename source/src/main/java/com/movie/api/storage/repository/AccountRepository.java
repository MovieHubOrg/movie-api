package com.movie.api.storage.repository;

import com.movie.api.storage.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findFirstByUsernameAndStatusNot(String username, Integer status);

    Optional<Account> findFirstByEmailAndStatusNot(String username, Integer status);

    Optional<Account> findFirstByEmail(String email);

    Optional<Account> findFirstByEmailAndStatus(String email, Integer status);

    boolean existsByUsernameAndStatusNot(String username, Integer status);

    boolean existsByEmailAndStatusNot(String email, Integer status);

    boolean existsByPhoneAndStatusNot(String phone, Integer status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Account a WHERE a.status = 0 AND a.createdDate <= :date")
    void deleteUserPendingBeforeDate(@Param("date") Date date);

    Optional<Account> findByIdAndStatusAndKind(Long id, Integer status, Integer kind);
}
