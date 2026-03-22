package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "reaction")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Reaction extends Auditable<String> {
    private Long userId;

    private Integer type;

    private Long commentId;

    private Long reviewId;
}
