package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "notification")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Notification extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private String title;

    private String cmd;

    @Column(columnDefinition = "LONGTEXT")
    private String body; // internal data

    private Integer type;

    private Boolean isRead = false;
}
