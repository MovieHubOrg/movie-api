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
import java.util.Date;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "notification_template")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class NotificationTemplate extends Auditable<String> {
    private String title;

    private String cmd;

    @Column(columnDefinition = "LONGTEXT")
    private String body; // internal data

    private Integer type;

    private Integer targetType; // 1: app, 2: specific accounts, 3: segment

    @Column(columnDefinition = "LONGTEXT")
    private String targetValue; // app name, accountIds, or segment marker

    private Date scheduleAt;
}
