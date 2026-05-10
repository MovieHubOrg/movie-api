package com.movie.api.storage.repository;

import com.movie.api.storage.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    @Query("SELECT n FROM Notification n WHERE n.id IN :ids AND n.account.id = :accountId")
    List<Notification> findAllByIdInAndAccountId(@Param("ids") List<Long> ids, @Param("accountId") Long accountId);

    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.account.id = :accountId")
    Optional<Notification> findByIdAndAccountId(@Param("id") Long id, @Param("accountId") Long accountId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.account.id = :accountId AND n.isRead = false")
    long countUnreadByAccountId(@Param("accountId") Long accountId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.account.id = :accountId AND n.isRead = false")
    void markAllUnreadAsReadByAccountId(@Param("accountId") Long accountId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.account.id = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);
}
