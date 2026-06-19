package com.movie.api.storage.repository;

import com.movie.api.storage.model.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long>, JpaSpecificationExecutor<UserReport> {
    boolean existsByUserIdAndObjectIdAndType(Long userId, Long objectId, Integer type);
    void deleteByTypeAndObjectId(Integer type, Long objectId);
}
