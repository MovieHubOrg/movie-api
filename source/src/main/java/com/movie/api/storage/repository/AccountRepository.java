package com.movie.api.storage.repository;

import com.movie.api.storage.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByIdAndStatusAndKind(Long id, Integer status, Integer kind);

    List<Account> findAllByStatusAndKindIn(Integer status, List<Integer> kind);

    List<Account> findAllByIdInAndStatus(List<Long> ids, Integer status);

    List<Account> findAllByIdInAndKindAndStatus(List<Long> ids, Integer kind, Integer status);

    @Query("select count(a) from Account a " +
            "where a.status = :status " +
            "and a.kind = :kind " +
            "and (:fromDate is null or a.createdDate >= :fromDate) " +
            "and (:toDate is null or a.createdDate <= :toDate)")
    Long countByStatusAndKindAndCreatedDateBetween(@Param("status") Integer status,
                                                   @Param("kind") Integer kind,
                                                   @Param("fromDate") Date fromDate,
                                                   @Param("toDate") Date toDate);
}
