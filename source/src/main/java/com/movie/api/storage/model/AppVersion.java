package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "app_version")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class AppVersion extends Auditable<String> {
    private Integer code;
    private String name;
    private String filePath;
    private Boolean forceUpdate = false;
    @Column(columnDefinition = "TEXT")
    private String changeLog;
    private Boolean isLatest = false;
}
