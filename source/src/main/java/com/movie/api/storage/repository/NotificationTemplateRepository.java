package com.movie.api.storage.repository;

import com.movie.api.storage.model.NotificationTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long>, JpaSpecificationExecutor<NotificationTemplate> {
    List<NotificationTemplate> findAllByStatusAndScheduleAtLessThanEqual(Integer status, Date scheduleAt, Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM NotificationTemplate nt WHERE nt.status = :status")
    void deleteByStatus(@Param("status") Integer status);
}
