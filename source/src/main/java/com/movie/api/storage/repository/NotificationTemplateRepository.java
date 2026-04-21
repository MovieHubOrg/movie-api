package com.movie.api.storage.repository;

import com.movie.api.storage.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;
import java.util.List;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long>, JpaSpecificationExecutor<NotificationTemplate> {
    List<NotificationTemplate> findAllByStatusAndScheduleAtLessThanEqual(Integer status, Date scheduleAt);
}
