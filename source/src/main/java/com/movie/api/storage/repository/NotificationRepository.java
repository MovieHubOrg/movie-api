package com.movie.api.storage.repository;

import com.movie.api.storage.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    @Query("SELECT n FROM Notification n WHERE n.id IN :ids AND n.account.id = :accountId")
    List<Notification> findAllByIdInAndAccountId(@Param("ids") List<Long> ids, @Param("accountId") Long accountId);
}
