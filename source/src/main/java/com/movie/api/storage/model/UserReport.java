package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "user_report")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class UserReport extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Account user;

    private Long objectId;

    private Integer type; // 1: comment, 2: review

    @Column(columnDefinition = "TEXT")
    private String content;
}
