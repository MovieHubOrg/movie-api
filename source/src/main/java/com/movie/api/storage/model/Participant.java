package com.movie.api.storage.model;

import com.movie.api.constant.DatabaseConstant;
import com.movie.api.storage.base.Auditable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "participant")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Participant extends Auditable<String> {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Account user;

    private Integer role = 0; // 0: guest, 1: host

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private Integer state = 0; // 0: pending, 1: join, 2: left
}
